# MechFind

> 🛠️ **AI-Powered Mobile Platform for Locating Local Mechanic Services and Vehicle Repair Solutions**
>
> Connect vehicle owners with nearby mechanics and tow truck services instantly. Diagnose vehicle problems with AI, find specialists, and book services seamlessly.

<div align="center">

![MechFind](https://img.shields.io/badge/MechFind-Vehicle%20Service%20Platform-orange?style=for-the-badge&logo=tools)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Firebase](https://img.shields.io/badge/Firebase-Cloud%20DB-FFCA28?style=for-the-badge&logo=firebase&logoColor=white)](https://firebase.google.com/)
[![React](https://img.shields.io/badge/React-Web%20Platform-61DAFB?style=for-the-badge&logo=react&logoColor=white)](https://reactjs.org/)
[![Android](https://img.shields.io/badge/Android-Mobile%20App-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge&logo=opensourceinitiative&logoColor=white)](LICENSE)

*Revolutionizing roadside assistance by combining AI diagnostics with geolocation-based mechanic discovery*

[Overview](#-overview) • [Features](#-features) • [Tech Stack](#-tech-stack) • [Quick Start](#-quick-start) • [System Architecture](#-system-architecture) • [Database](#-database) • [API Services](#-api-services) • [Team](#-team)

</div>

---

## 🎯 Overview

**MechFind** is an innovative mobile and web platform that solves the critical problem of vehicle breakdowns by combining artificial intelligence with geolocation services. When drivers encounter unexpected mechanical failures, they can:

1. **Get Instant AI Diagnosis** - Describe symptoms to our AI chatbot for troubleshooting guidance
2. **Find Nearby Mechanics** - Locate specialists within their vicinity using GPS
3. **Filter by Specialization** - Find mechanics experienced with their specific vehicle model
4. **Book & Pay** - Arrange services directly through the app with secure payments
5. **Track Tow Trucks** - Request and monitor tow truck services in real-time

### Perfect for:
- 🚗 **Vehicle Owners** - Quick, reliable access to local repair services during emergencies
- 🔧 **Mechanics & Repair Shops** - Receive immediate service requests and manage bookings
- 🚙 **Tow Truck Operators** - Get dispatch requests for vehicle recovery and transport
- 👨‍💼 **Service Administrators** - Track analytics, ratings, and feedback across the platform

---

## ✨ Features

### 🤖 AI Diagnosis Module
- **Intelligent Chatbot** - Natural language processing for vehicle symptom analysis
- **Vehicle Issue Detection** - AI-powered identification of common mechanical problems
- **Troubleshooting Guidance** - Step-by-step repair instructions for minor issues
- **Chat History** - Store and review past conversations for future reference
- **Multi-Symptom Analysis** - Analyze multiple symptoms simultaneously for accurate diagnosis

### 🗺️ Location & Discovery Services
- **Live GPS Tracking** - Real-time user location capture without manual address entry
- **Geospatial Search Algorithm** - Find nearest mechanics and tow trucks based on proximity
- **Interactive Maps Integration** - Google Maps API for visual service provider locations
- **Distance Calculation** - Automatic distance computation to nearby services
- **Location-Based Filtering** - Show services within specified radius

### 🔧 Mechanic Discovery & Management
- **Verified Mechanic Profiles** - Detailed information about registered mechanics
- **Specialization Filtering** - Filter by vehicle brand (Toyota, BMW, Suzuki, etc.)
- **Availability Status** - Real-time display of mechanic availability
- **Service Category Listing** - Browse specific repair services offered
- **Mechanic Ratings & Reviews** - View historical customer feedback and star ratings
- **Service Pricing** - Transparent cost information for different services

### 🚙 Tow Truck Services (TowMan)
- **Tow Truck Discovery** - Locate available towing services nearby
- **Tow Truck Types & Sizes** - Filter by truck specifications needed
- **Real-time Availability** - Check if tow services are currently available
- **Emergency Dispatch** - Quick booking for urgent towing needs
- **Vehicle Transport Tracking** - Monitor tow truck location en route

### 📋 Booking & Request Management
- **One-Click Booking** - Seamless service request creation
- **Mechanic Booking Requests** - Schedule repairs with selected mechanics
- **Tow Truck Booking Requests** - Request vehicle recovery services
- **Booking Status Tracking** - Real-time updates on request progress
- **Booking History** - View past and current service requests
- **Request Cancellation** - Easy cancellation with appropriate status management
- **Booking Confirmation** - Instant notifications upon booking acceptance

### 💳 Secure Payment System
- **In-App Payments** - Process transactions directly within the application
- **Multiple Payment Methods** - Support for various payment gateways
- **Payment API Integration** - Secure third-party payment processing
- **Transaction History** - Complete payment records and receipts
- **Card Information Security** - Encrypted storage of payment details
- **Booking-Linked Payments** - Associate payments with specific service bookings

### ⭐ Feedback & Rating System
- **Post-Service Ratings** - 5-star rating system for completed services
- **Detailed Reviews** - Write comprehensive feedback about mechanic experience
- **Order-Linked Feedback** - Link reviews to specific service bookings
- **Rating Aggregation** - Calculate average ratings per mechanic
- **Feedback History** - View all reviews and ratings over time
- **Helpfulness Voting** - Community rating of review usefulness

### 👥 User Management
- **Vehicle Owner Profiles** - Personal information and vehicle details storage
- **Mechanic Profiles** - Professional credentials and specialization info
- **Tow Truck Operator Profiles** - Business details and service information
- **User Authentication** - Secure login and password management
- **Profile Editing** - Update personal and professional information
- **Gender & Contact Details** - Complete demographic tracking

### 📊 Data Management & Analytics
- **Comprehensive User Database** - Centralized user information storage
- **Vehicle Information Catalog** - Track vehicle types and specifications
- **Mechanic Specialization Records** - Database of expertise and services
- **Booking Analytics** - Track booking trends and patterns
- **Payment Records** - Complete transaction audit trail
- **Feedback Analytics** - Analyze customer satisfaction trends

### 🔐 Backend & Infrastructure
- **RESTful API Architecture** - Clean, standardized API endpoints
- **Cloud Database Hosting** - Firebase for scalable data storage
- **Cloud Server Infrastructure** - AWS for reliable backend hosting
- **Version Control** - GitHub for code management and collaboration
- **API Testing & Monitoring** - Postman integration for quality assurance
- **Multi-platform Support** - Android mobile app and web interface

### 📱 Multi-Platform Access
- **Android Mobile Application** - Native mobile app for on-the-go access
- **Responsive Web Platform** - Browser-based access for desktop users
- **Cross-Platform Synchronization** - Seamless data sync between platforms
- **Offline Capability** - Cache critical data for offline access

---

## 🛠️ Tech Stack

### Frontend
| Technology | Purpose |
|-----------|---------|
| **XML** | Android UI and layout design |
| **React** | Web application development |
| **HTML5/CSS3** | Web interface markup and styling |
| **Android Studio** | Mobile application development environment |

### Backend
| Technology | Purpose |
|-----------|---------|
| **Java** | Complex business logic and application functions |
| **PHP** | Website backend scripting |
| **Spring Boot** | Backend framework (if used) |

### Database & Cloud
| Technology | Purpose |
|-----------|---------|
| **Firebase** | Cloud database for mobile-backend communication |
| **AWS** | Secure cloud servers for backend hosting |
| **Free Cloud Host** | Website publishing and hosting |

### APIs & Services
| Service | Purpose |
|---------|---------|
| **Google Maps API** | Live location tracking and geospatial search |
| **GPS Location Services** | Automatic user coordinate capture |
| **Payment API (Payhere)** | Secure payment processing |
| **Open Street Map API** | Alternative location services |

### Development Tools
| Tool | Purpose |
|------|---------|
| **GitHub** | Version control and code backup |
| **Postman** | API testing and documentation |
| **VS Code** | Frontend and backend code editor |
| **Android Studio** | Mobile app development IDE |

---

## 🏗️ System Architecture

### High-Level Architecture
```
┌─────────────────┐
│  Mobile App     │
│  (Android/XML)  │
└────────┬────────┘
         │
         ├──────────────┐
         │              │
┌────────▼─────┐  ┌────▼─────────┐
│ GPS & Maps   │  │  AI Chatbot   │
│  (Google API)│  │  (NLP Engine) │
└────┬─────────┘  └──────┬────────┘
     │                   │
     └───────────┬───────┘
                 │
         ┌───────▼─────────┐
         │  Spring Boot    │
         │  Backend (Java) │
         └───────┬─────────┘
                 │
         ┌───────▼──────────┐
         │  Firebase Cloud  │
         │  Database        │
         └──────────────────┘

┌──────────────────┐
│   Web Platform   │
│ (React/HTML/CSS) │
└────────┬─────────┘
         │
         └────────┘
             │
      ┌──────▼────────────┐
      │  AWS Backend      │
      │  Services & APIs  │
      └───────────────────┘
```

### User Flow
1. **Vehicle Owner** opens MechFind app/web platform
2. **Enters symptoms** into AI Chatbot
3. **Receives diagnosis** and troubleshooting advice
4. **Enables GPS** for location services
5. **Searches nearby mechanics** (geospatial algorithm filters results)
6. **Filters by specialization** (vehicle model/service type)
7. **Views ratings & reviews** from other users
8. **Selects mechanic** and books service
9. **Processes payment** securely in-app
10. **Receives confirmation** and tracking updates
11. **Provides feedback** post-service completion

---

## 📊 Database Schema

### Core Tables

**User Table**
```
UserID (PK) | Name | Contact | Gender | Password | Email | Location | UserType
```

**VehicleOwner Table**
```
VehicleOwnerId (PK) | VehicleInformation
```

**Mechanic Table**
```
MechanicID (PK) | Specialization | Availability
```

**TowMan Table**
```
TowManID (PK) | TowTruckType | TowTruckSize | Availability
```

**ChatHistory Table**
```
ChatId (PK) | UserID (FK) | AIResponses
```

**MechanicBooking Table**
```
MechanicBookingId (PK) | VehicleOwnerId (FK) | MechanicID (FK) | BookingStatus | BookingDate
```

**TowTruckBooking Table**
```
TruckBookingId (PK) | VehicleOwnerId (FK) | TowManID (FK) | BookingStatus | BookingDate
```

**Payment Table**
```
PaymentId (PK) | BookingId (FK) | CardNumber | ExpiryDate | CVV | Address | PostalCode
```

**Feedback Table**
```
FeedbackID (PK) | Description | Rating | BookingId (FK)
```

---

## 🌐 API Services

### Key API Endpoints

**Mechanic Search**
- `GET /api/mechanics/nearby` - Find nearby mechanics with GPS coordinates
- `GET /api/mechanics/filter` - Filter mechanics by specialization
- `GET /api/mechanics/{id}` - Get detailed mechanic profile

**Booking Management**
- `POST /api/bookings/mechanic` - Create mechanic booking
- `POST /api/bookings/tow-truck` - Create tow truck booking
- `GET /api/bookings/status/{id}` - Check booking status
- `PUT /api/bookings/{id}/cancel` - Cancel booking

**Feedback & Ratings**
- `POST /api/feedback` - Submit review and rating
- `GET /api/feedback/mechanic/{id}` - Get mechanic reviews
- `GET /api/ratings/average/{mechanicId}` - Get average rating

**Payment Processing**
- `POST /api/payments/process` - Process payment
- `GET /api/payments/history/{userId}` - View payment history

**AI Chatbot**
- `POST /api/chat/diagnose` - Send symptom and get diagnosis
- `GET /api/chat/history/{userId}` - Retrieve chat history

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Android Studio (for mobile development)
- Node.js & npm (for React web app)
- Firebase account
- AWS account (optional, for backend hosting)
- MySQL 8.0+

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Navinduuu/MechFind.git
   cd MechFind
   ```

2. **Setup Backend (Java/Spring Boot)**
   ```bash
   cd backend
   mvn install
   mvn spring-boot:run
   ```

3. **Setup Mobile App (Android)**
   ```bash
   Open Android Studio
   Import project folder
   Build and run on emulator/device
   ```

4. **Setup Web Platform (React)**
   ```bash
   cd frontend
   npm install
   npm start
   ```

5. **Configure Firebase**
   - Add Firebase credentials to configuration files
   - Enable Firestore database and authentication

6. **Configure APIs**
   - Add Google Maps API key
   - Setup payment gateway credentials
   - Configure AWS S3/EC2 (if using)

---

## 📋 Development Methodology

**Agile Software Development** with sprint-based iterations:
- ✅ Iterative development with continuous feedback
- ✅ Flexible requirement adaptation
- ✅ Regular testing in each sprint cycle
- ✅ Continuous integration and deployment

### Testing Strategies
- **Unit Testing** - Individual component validation
- **Integration Testing** - Module interaction verification
- **System Testing** - End-to-end functionality testing
- **User Acceptance Testing** - Real-world scenario validation
- **Performance Testing** - Load and stress testing

---

## 👥 Target Audience

### Primary Users
- Vehicle owners with technical knowledge gaps
- Drivers requiring emergency roadside assistance
- Users seeking convenient mechanic booking

### Secondary Users
- Auto repair shops and mechanics
- Tow truck service operators
- Service administrators and analytics teams

---

## 📈 Estimated Cost & Resources

### Infrastructure (Free Tier Options)
- AWS Free Tier for backend hosting
- Firebase free version for database
- Vercel or free cloud host for website
- Open Street Map API (free alternative to Google Maps)
- Open source development tools

### Third-Party Services
- Payment Gateway (Payhere) - Per-transaction fees
- Google Maps API - Usage-based pricing (if not using Open Street Map)
- AWS Paid Services - If exceeding free tier limits

---

## 📝 Project Timeline

| Phase | Date | Activity |
|-------|------|----------|
| 1 | May 09, 2026 | Project discussion & requirement gathering |
| 2 | May 17, 2026 | System analysis and planning |
| 3 | May 23, 2026 | UML diagrams & database design |
| 4 | June 06, 2026 | UI/UX design |
| 5 | June 07, 2026 | Proposal preparation |
| 6 | Ongoing | Development & testing sprints |

---

## 🎓 Learning Outcomes

Through MechFind development, the team gains experience in:
- ✓ Agile software development methodology
- ✓ Frontend engineering with XML, React, HTML5, CSS3
- ✓ Backend development with Java and PHP
- ✓ Cloud database management with Firebase
- ✓ Third-party API integration (Google Maps, Payment APIs)
- ✓ Cloud infrastructure deployment (AWS)
- ✓ Version control and CI/CD with GitHub

---

## 👨‍💼 Team

MechFind is developed by a dedicated team of software engineers and developers:

| Member | Role | GitHub |
|--------|------|--------|
| **Yasindu Fernando** | Full Stack Developer | [@YasinduFdo](https://github.com/YasinduFdo) |
| **Nethusha Ilukpitiya** | Backend & Database Developer | - |
| **Navindu Fernando** | Mobile & Frontend Developer | [@Navinduuu](https://github.com/Navinduuu) |
| **M L D Dananjaya** | AI/ML Engineer & System Architect | - |

We're passionate about revolutionizing roadside assistance through innovative technology and seamless user experiences.

---

## 📚 References

1. Pérez-Vázquez, A., Anzures-García, M., & Sánchez-Gálvez, L. A. (2024). Vehicle Engine Fault Diagnosis Approach Based on a Decision Tree and Knowledge Base. *International Journal of Combinatorial Optimization Problems & Informatics*, 15(2), 185–194.

2. Pavlopoulos, J., Romell, A., Curman, J., et al. (2024). Automotive fault nowcasting with machine learning and natural language processing. *Machine Learning*, 113, 843–861.

3. Hossain, M. N., Rahman, M. M., & Ramasamy, D. (2024). Artificial Intelligence-Driven Vehicle Fault Diagnosis to Revolutionize Automotive Maintenance: A Review. *Computer Modeling in Engineering & Sciences*, 141(2), 951–996.

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

We welcome contributions! Please feel free to submit a Pull Request to help improve MechFind.

---

## 📞 Support

For issues, feature requests, or questions, please open a [GitHub Issue](https://github.com/Navinduuu/MechFind/issues).

---

<div align="center">

**Made with ❤️ by the MechFind Team**

*Transforming roadside assistance through AI and geolocation technology*

</div>
