/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  ricky
 * Created: 6 Mar 2025
 */
CREATE DATABASE MyCalendarDB;
USE MyCalendarDB;

CREATE TABLE Events(
    Id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(50),
    description varchar(200),
    date DATETIME,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(10,8)
    location varchar(255)
)