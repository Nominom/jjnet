CREATE TABLE t_advertisements (
	id int primary key auto_increment not null ,
	hash varbinary not null,
	advertisement varbinary not null
);

CREATE TABLE t_classes (
	id int primary key auto_increment not null,
	class varchar not null
);

CREATE TABLE t_advertisement_classes (
	advertisement_id int not null,
	class_id int not null,
	FOREIGN KEY (advertisement_id) REFERENCES t_advertisements(id) ON DELETE CASCADE,
	FOREIGN KEY (class_id) REFERENCES t_classes(id) ON DELETE CASCADE
);