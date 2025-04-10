# 🎟️ Ticketing-Web-App-Vistaseat.com

An integrated **seat reservation and ticketing system** for events and performances, developed as part of my MSc thesis in Informatics.

> 🌐 A full-stack Java web application combining MVC architecture and RESTful services.

## 🗂️ Project Overview
**Vistaseat.com** is a web-based ticketing platform that facilitates the exploration, reservation, and purchase of tickets for a variety of events, including:

- Museum visits 🏛️
- Archaeological sites 🏺
- Theater plays 🎭
- Cinema films 🎬
- Music concerts and festivals 🎶
- Sporting events ⚽

## 🧰 Core Features
- 🔎 **Event Exploration & Search:** Filter by category, date, or location
- 🧾 **Reservation & Purchase:** Select venue seats and generate unique ticket
- 🗃️ **User Account Management:** View ticket history and update info
- 🛠️ **Admin Panel:** Add/edit/delete events, users, and venues

## 🔧 Technologies Used

### 🖥️ Backend:
- **Java 21**
- **Spring Framework** (Core, MVC, Security, Data JPA)
- **Spring Boot**
- **REST API** design & implementation
- **PostgreSQL** with Hibernate ORM
- **BCrypt** for password hashing

### 🎨 Frontend:
- **HTML5, CSS3, JavaScript**
- **Thymeleaf** templating engine
- **Responsive UI**

## 🔐 Authentication & Authorization
- Role-based access control using Spring Security
- Passwords are hashed using **BCryptPasswordEncoder**
- Stateful authentication and session creation to maintain user authentication state

## 👥 User Roles
- **Guest:** Can browse, search events and purchase tickets without signing in
- **Registered User:** Can book seats, purchase tickets, and manage profile
- **Admin:** Has access to an admin dashboard to manage users, venues, and events

## 🧩 Architecture Highlights
- **MVC + REST APIs:** Clean separation of concerns with REST endpoints to expose data.
- **PostgreSQL:** Relational design with relationships between Users, Events, and Venues handled with JPA & Hibernate ORM

## 🎓 Academic Context
This app is part of my MSc Dissertation at the University of Pireaus - Department of Informatics, titled:
> Design and Development of an Integrated Ticketing and Seat Reservation System
> 
The goal is to simulate a real-world ticketing platform while showcasing skills in software engineering,
database design, and full-stack development.

## 🔄 Status
🛠️ The project is a work in progress — continuously developed and expanded with more features and polish.

## 📋 Final Note
Vistaseat.com is a comprehensive ticketing platform that demonstrates the practical value of modern web technologies
in streamlining event management.
Designed with scalability, usability, and real-world application in mind,
the system lays a solid foundation for further development and potential deployment in live environments.

> 📬 I'd love your feedback or suggestions for improvement — feel free to connect!

---

**&copy; 2025 Georgios Simos **

