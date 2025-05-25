CREATE DATABASE MyCalendarDB;
USE MyCalendarDB;

CREATE TABLE Events(
    Id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(50) NOT NULL,
    description varchar(200) NOT NULL,
    date DATETIME NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(10,8) NOT NULL,
    location varchar(50)
)