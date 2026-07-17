drop schema if exists insurance_db;
create schema insurance_db;
use insurance_db;

create table agent(
agent_id INT AUTO_INCREMENT,
name VARCHAR(50),
email VARCHAR(100) UNIQUE,
contact VARCHAR(15),
password VARCHAR(60),
role VARCHAR(10),
CONSTRAINT agent_id_pk PRIMARY KEY (agent_id)
);

create table customer(
customer_id INT AUTO_INCREMENT,
name VARCHAR(50),
email VARCHAR(100) UNIQUE,
contact VARCHAR(15),
password VARCHAR(60),
role VARCHAR(10),
agent_id INT,
FOREIGN KEY(agent_id) REFERENCES agent(agent_id),
CONSTRAINT customer_id_pk PRIMARY KEY (customer_id)
);

create table policy(
policy_id VARCHAR(10),
policy_type VARCHAR(30),
policy_start_date DATE,
policy_end_date DATE,
last_premium_payment_date DATE,
premium_amount DOUBLE,
policy_status VARCHAR(10),
nominee VARCHAR(50),
customer_id INT,
FOREIGN KEY(customer_id) REFERENCES customer(customer_id),
CONSTRAINT policy_id_pk PRIMARY KEY (policy_id)
);

INSERT INTO agent VALUES(1,'Sai Girish','girishbankupalli@gmail.com',9323456712,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');
INSERT INTO agent VALUES(2,'Kiranmayee','kiranmayee.t123@gmail.com',9323456368,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');
INSERT INTO agent VALUES(3,'Vithika','vithika12@gmail.com',8323456567,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');
INSERT INTO agent VALUES(4,'Saksham','saksham@gmail.com',7563456812,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');
INSERT INTO agent VALUES(5,'Lakshman','laksham12@gmail.com',9563456815,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');
INSERT INTO agent VALUES(6,'Venkat Ram','venkat_ram446@gmail.com',9563456416,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');
INSERT INTO agent VALUES(7,'Rahul Sharma','rahul31@gmail.com',8656345641,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');
INSERT INTO agent VALUES(8,'Kiran Babu','kiran_456@gmail.com',9656367175,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');
INSERT INTO agent VALUES(9,'Swathi','swathi_b@gmail.com',7656398129,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');
INSERT INTO agent VALUES(10,'Kiran Kumar','kiran_kr@gmail.com',9656393176,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','AGENT');

INSERT INTO customer VALUES(1,'Vivaan Kashyap','vivvan12@gmail.com',9512603284,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',5);
INSERT INTO customer VALUES(2,'Arshad Mapari','arshadhm200@gmail.com',8512603194,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',1);
INSERT INTO customer VALUES(3,'Nitin Kumar','Nitin_kumar@gmail.com',7912603168,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',7);
INSERT INTO customer VALUES(4,'Vaishali Sharma','Vaishali_sh@gmail.com',6312603927,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',1);
INSERT INTO customer VALUES(5,'Meghana Raj','Meghana_R@gmail.com',9312603414,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',9);
INSERT INTO customer VALUES(6,'Anurag Mishra','Anurag_Mishra@gmail.com',8312603501,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',4);
INSERT INTO customer VALUES(7,'Kusuma','Kusuma_B@gmail.com',6012603263,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',8);
INSERT INTO customer VALUES(8,'Karunya','Karunyaa@gmail.com',9848011349,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',10);
INSERT INTO customer VALUES(9,'Kapil Sharma','Kapil_KS@gmail.com',8885532210,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',3);
INSERT INTO customer VALUES(10,'Vinod Vamsi','Vinod_VS@gmail.com',9251703572,'$2a$10$y3dtgfhCkiVx74cGjxzJVu2iCSdqqOgknzd4YM34JN9mPhkWY8jCK','CUSTOMER',6);

INSERT INTO policy values('POL-101','LIFE_INSURANCE','2025-03-02','2030-03-02','2026-01-02', 1000, 'LAPSED', 'Karan', 2);
INSERT INTO policy values('POL-102','HEALTH_INSURANCE','2026-01-22','2036-01-22','2026-06-02', 1000, 'ACTIVE', 'Sravan', 2);
INSERT INTO policy values('POL-103','HEALTH_INSURANCE','2024-12-22','2036-03-02','2026-01-02', 3000, 'LAPSED', 'Kiara', 3);
INSERT INTO policy values('POL-104','HOME_INSURANCE','2020-03-02','2040-03-02','2026-06-10', 5000, 'ACTIVE', 'Malini', 2);
INSERT INTO policy values('POL-105','VEHICLE_INSURANCE','2025-03-02','2030-03-02','2026-01-10', 3000, 'LAPSED', 'Shyam', 5);
INSERT INTO policy values('POL-106','HOME_INSURANCE','2025-07-09','2065-03-02','2025-12-10', 5000, 'LAPSED', 'Meera', 6);
INSERT INTO policy values('POL-107','LIFE_INSURANCE','2026-03-02','2040-03-12','2026-04-10', 2000, 'LAPSED', 'Suma', 7);
INSERT INTO policy values('POL-108','HEALTH_INSURANCE','2025-01-02','2030-03-02','2026-02-10', 5000, 'LAPSED', 'Megha', 9);
INSERT INTO policy values('POL-109','VEHICLE_INSURANCE','2026-03-22','2031-03-02','2026-06-12', 3000, 'ACTIVE', 'Ram', 2);
INSERT INTO policy values('POL-110','HEALTH_INSURANCE','2025-03-02','2030-03-02','2025-10-10', 1000, 'LAPSED', 'Chandra', 10);

select * from agent;
select * from customer;
select * from policy;