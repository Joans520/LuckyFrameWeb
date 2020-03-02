package com.luckyframe.common.netty;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.luckyframe.common.constant.JobConstants;
import com.luckyframe.common.constant.ScheduleConstants;
import com.luckyframe.framework.config.LuckyFrameConfig;
import com.luckyframe.project.monitor.job.domain.Job;
import com.luckyframe.project.monitor.job.mapper.JobMapper;
import com.luckyframe.project.monitor.job.util.ScheduleUtils;
import com.luckyframe.project.system.client.domain.Client;
import com.luckyframe.project.system.client.mapper.ClientMapper;
import com.luckyframe.project.system.client.service.IClientService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@Component("ServerHandler")
@ChannelHandler.Sharable
public class ServerHandler extends ChannelHandlerAdapter {

    @Resource
    private ClientMapper clientMapper;

    @Resource
    private LuckyFrameConfig lfConfig;

    @Resource
    private IClientService clientService;

    @Resource
    private NettyChannelMap nettyChannelMap;

    @Resource
    private JobMapper jobMapper;

    @Resource
    private Scheduler scheduler;

    private static String file_dir = System.getProperty("user.dir")+"/tmp";

    protected ChannelHandlerContext ctx;

    private CountDownLatch latch;

    private int byteRead;

    private volatile int start = 0;

    /**
     * 消息的唯一ID
     */
    private String unidId = "";
    /**
     * 同步标志
     */
    protected int rec;
    /**
     * 客户端返回的结果
     */
    private Result result;
     /**
     * 心跳丢失次数
     */
    private int counter = 0;


    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        JSONObject json= JSON.parseObject(msg.toString());
        /*
        * ClientUp客户端启动，自动注册到服务端中
        * */
        if("clientUp".equals(json.get("method")))
        {
            if(nettyChannelMap.get(json.get("hostName").toString())!=null)
            {
                JSONObject tmp=new JSONObject();
                tmp.put("method","return");
                tmp.put("message","客户端名称重复，自动注册失败");
                tmp.put("success","-1");
                ctx.writeAndFlush(tmp.toString());
                log.error("客户端host.name重复，注册失败:"+json.get("hostName").toString());
                //登录失败，断开连接
                ctx.close();
                return;
            }
            ChannelMap.setChannel(json.get("hostName").toString(),ctx.channel());
            ChannelMap.setChannelLock(json.get("hostName").toString(),new ReentrantLock());
            //返回接受成功消息
            JSONObject tmp=new JSONObject();
            tmp.put("method","return");
            tmp.put("message","收到消息");
            tmp.put("success","1");
            ctx.writeAndFlush(tmp.toString());

            //接收到客户端上线消息
            log.info("#############客户端上线##############");
            log.info("上线主机名为："+json.get("hostName"));
            String hostName=new String(json.get("hostName").toString().getBytes("UTF-8"),"UTF-8");
            //检查客户端是否已经注册入库
            Client client=clientService.selectClientByClientIP(hostName);
            if(client==null)
            {
                client = new Client();
                client.setClientIp(hostName);
                client.setClientName(hostName);
                client.setCheckinterval(30);
                client.setClientPath("/TestDriven");
            }
            if(client.getClientId()==null)
            {
                //未注册则自动注册入库
                Job job=new Job();
                job.setJobName(JobConstants.JOB_JOBNAME_FOR_CLIENTHEART);
                job.setJobGroup(JobConstants.JOB_GROUPNAME_FOR_CLIENTHEART);
                job.setMethodName(JobConstants.JOB_METHODNAME_FOR_CLIENTHEART);
                job.setMethodParams(client.getClientIp());
                job.setCronExpression("0/"+client.getCheckinterval().toString()+" * * * * ? ");
                job.setMisfirePolicy(ScheduleConstants.MISFIRE_DO_NOTHING);
                job.setStatus(JobConstants.JOB_STATUS_FOR_CLIENTHEART);
                job.setRemark("");
                /*在公共调度表中插入数据*/
                int result = jobMapper.insertJob(job);
                if(result<1){
                    //登录失败，断开连接
                    ctx.close();
                    throw new Exception("新增客户端时无法插入任务调度表");
                }
                //更新jobLis
                CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, job.getJobId());
                // 如果不存在，则创建
                if (cronTrigger == null)
                {
                    ScheduleUtils.createScheduleJob(scheduler, job);
                }
                else
                {
                    ScheduleUtils.updateScheduleJob(scheduler, job, cronTrigger);
                }
                /*在调度预约表中插入数据*/
                client.setJobId(job.getJobId().intValue());
                client.setClientIp(hostName);
                client.setClientName(hostName);
                clientService.insertClient(client);
                log.info("主机名为："+json.get("hostName")+"自动注册成功");
            }
            if(lfConfig.getVersion().equals(json.get("version"))){
                //版本号一致
                client.setClientIp(hostName);
                client.setRemark("检测客户端状态成功");
                client.setStatus(0);
                if(client.getClientId()!=null)
                    clientMapper.updateClient(client);
                else
                    clientMapper.insertClient(client);
                //登录成功,把channel存到服务端的map中
                nettyChannelMap.add(hostName,(SocketChannel)ctx.channel());
                //登陆成功，放入map中用于心跳
                NettyServer.clientMap.put(hostName,"0");
            }else{
                client.setClientIp(hostName);
                client.setRemark("客户端("+json.get("version")+")与服务器("+lfConfig.getVersion()+")版本不一致");
                client.setStatus(1);
                if(client.getClientId()!=null)
                    clientMapper.updateClient(client);
                else
                    clientMapper.insertClient(client);
                //登陆失败，删除心跳map中的数据
                NettyServer.clientMap.remove(hostName);
                //登录失败，断开连接
                ctx.close();
            }
        }
        else if("return".equals(json.get("method")))
        {
            /*
            * 向客户端请求后返回的数据
            * */
            Result re =JSONObject.parseObject(json.get("data").toString(),Result.class);
            //校验返回的信息是否是同一个信息
            if (unidId.equals(re.getUniId())){
                latch.countDown();//消息返回完毕，释放同步锁，具体业务需要判断指令是否匹配
                rec = 0;
                result = re;
            }
        }
        else if("ping".equals(json.get("method")))
        {
            /*
            * 客户端心跳检测
            * */
            String hostName=json.get("hostName").toString();
            //检查客户端是否已经注册入库
            Client client=clientService.selectClientByClientIP(hostName);
            if(NettyServer.clientMap.get(hostName)==null||(!"0".equals(NettyServer.clientMap.get(hostName)))||(0!=client.getStatus()))
            {
                //版本号一致
                client.setClientIp(hostName);
                client.setRemark("检测客户端状态成功");
                client.setStatus(0);
                clientMapper.updateClient(client);
                //更新客户端状态成功
                NettyServer.clientMap.put(hostName,"0");
            }
        }
        else if("upload".equals(json.get("method")))
        {
            Result re =JSONObject.parseObject(json.get("data").toString(),Result.class);
            FileUploadFile ef = re.getFileUploadFile();
            byte[] bytes = ef.getBytes();
            byteRead = ef.getEndPos();
            String md5 = ef.getFile_md5();//文件名
            String path = file_dir + File.separator + ef.getFile().getName();
            File file = new File(path);
            if(!file.getParentFile().exists())
            {
                file.getParentFile().mkdir();
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");//r: 只读模式 rw:读写模式
            randomAccessFile.seek(start);//移动文件记录指针的位置,
            randomAccessFile.write(bytes);//调用了seek（start）方法，是指把文件的记录指针定位到start字节的位置。也就是说程序将从start字节开始写数据
            start = start + byteRead;
            JSONObject tmp=new JSONObject();
            tmp.put("method","upload");
            tmp.put("success","1");
            tmp.put("uuid",json.get("uuid").toString());
            tmp.put("start",start);
            Map<String,Object> jsonparams =new HashMap<>();
            jsonparams.put("imgName",file.getName());
            tmp.put("data",jsonparams);
            ctx.writeAndFlush(tmp.toString());
            randomAccessFile.close();
            log.info("处理完毕,文件路径:"+path+","+byteRead);
        }
        //刷新缓存区
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx)  {
        //channel失效，从Map中移除
        nettyChannelMap.remove((SocketChannel)ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)){
                // 空闲40s之后触发 (心跳包丢失)
                if (counter >= 3) {
                    // 连续丢失3个心跳包 (断开连接)
                    ctx.channel().close().sync();
                    log.error("已与"+ctx.channel().remoteAddress()+"断开连接");
                } else {
                    counter++;
                    log.debug(ctx.channel().remoteAddress() + "丢失了第 " + counter + " 个心跳包");
                }
            }

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    public void resetSync(CountDownLatch latch, int rec) {
        this.latch = latch;
        this.rec = rec;
    }

    public void setUnidId(String s){
        this.unidId = s;
    }

    public Result getResult() {
        return result;
    }



}
