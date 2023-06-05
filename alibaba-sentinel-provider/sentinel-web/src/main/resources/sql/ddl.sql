create table if not exists t_order(
`id` int auto_increment primary key,
`user_id` int not null,
`time` datetime
)engine innodb;