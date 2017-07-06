create table PROJECT_CASE
(
  id                    int(8) not null AUTO_INCREMENT,
  sign                  VARCHAR(20) not null COMMENT '用例标识',
  name                  VARCHAR(200) not null COMMENT '用例名称',
  projectid             int(8) not null COMMENT '关联项目ID',
  time                  VARCHAR(30) COMMENT '最后更新时间',
  operationer           VARCHAR(20) COMMENT '最后更新人员',
  casetype              int(2) not null COMMENT '0 接口 1 UI',
  remark                VARCHAR(200) COMMENT '备注',
  primary key (ID)
)default character set utf8;

create table PROJECT_CASESTEPS
(
  id                    int(8) not null AUTO_INCREMENT,
  path                  VARCHAR(100) COMMENT '包路径|定位路径',
  operation             VARCHAR(100) COMMENT '方法名|操作',
  parameters            VARCHAR(500) COMMENT '参数',
  action                VARCHAR(50) COMMENT '步骤动作',
  caseid                int(8) not null COMMENT '用例ID',
  stepnum               int(2) not null COMMENT '步骤编号',
  expectedresult        VARCHAR(200) COMMENT '预期结果',
  projectid             int(8) not null COMMENT '项目ID',
  steptype              int(2) not null COMMENT '0 接口 1 UI',
  time                  VARCHAR(30)  COMMENT '最后更新时间',
  operationer           VARCHAR(20)  COMMENT '最后更新人员',
  remark                VARCHAR(200) COMMENT '备注',
  primary key (ID)
)default character set utf8;

create table PROJECT_PLAN
(
  id                    int(9) not null AUTO_INCREMENT,
  name                  VARCHAR(50) not null COMMENT '测试计划名称',
  casecount             int(8) COMMENT '计划中用例总数',
  remark                VARCHAR(200) COMMENT '备注',
  projectid             int(8) not null COMMENT '项目ID',
  time                  VARCHAR(30) COMMENT '最后更新时间',
  operationer           VARCHAR(20) COMMENT '最后更新人员',
  primary key (ID)
)default character set utf8;

create table PROJECT_PLANCASE
(
  id                    int(9) not null AUTO_INCREMENT,
  caseid                int(8) not null COMMENT '用例ID',
  planid                int(8) not null COMMENT '项目计划ID',
  priority              int(8) not null COMMENT '用例优先级',
  primary key (ID),
  index (caseid),
  index (planid) 
)default character set utf8;
/*用户增加默认选择字段*/
alter table userinfo add projectid int(4) default '0';
/*项目管理表增加项目标识字段*/
alter table QA_SECTORPROJECTS add projectsign VARCHAR(20) not null;