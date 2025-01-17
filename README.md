# Volunteering Platform API

## Project Description
The Volunteering Platform API provides a backend solution for managing events, users, and roles in a volunteering system. Users can sign up, log in, and perform various actions based on their roles (Admin, Organization, Volunteer). The platform also includes functionalities for creating events, approving users/events, and registering volunteers for events.

## Features
- **Role-based access control**:
  - Admins: Approve users, manage events.
  - Organizations: Create events, view registered volunteers.
  - Volunteers: Register for events, view approved events.
- **Event Management**: Create, approve, and manage events.
- **User Approval**: Manage user statuses (Pending, Approved).
- **Secure Authentication**: Password encryption with BCrypt and Spring Security.
- **API Documentation**: Integrated Swagger documentation for exploring the API.

## Setup Instructions

### Prerequisites
- Java 17+
- Maven
- Spring Boot
- PostgreSQL/MySQL (for production)
- H2 Database (for testing)

### Installation Steps
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd volunteering-platform
   ```
2. Configure the database in `application.properties` or `application.yml`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/volunteering
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# --- JPA / Hibernate ---
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

   ```
3. Build and run the project:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
4. Access Swagger documentation at: `http://localhost:8080/swagger-ui.html`

## Usage Instructions

### API Endpoints

#### Authentication
- **POST** `/api/auth/signup` - Register a new user.
- **GET** `/api/auth/login` - Log in and retrieve role-based options.

#### Admin Endpoints
- **GET** `/api/auth/admin/menu` - Access admin functionalities.
- **PUT** `/api/auth/admin/approve/{userId}` - Approve a user.
- **PUT** `/api/auth/admin/approve-event/{eventId}` - Approve an event.

#### Organization Endpoints
- **POST** `/api/auth/organization/create-event` - Create a new event.
- **GET** `/api/auth/organization/event-volunteers/{eventId}` - View volunteers registered for an event.

#### Volunteer Endpoints
- **GET** `/api/auth/volunteer/menu` - Access volunteer functionalities.
- **POST** `/api/auth/volunteer/register/{eventId}` - Register for an event.

### Role Definitions
- **Admin**: Manage platform and approve users/events.
- **Organization**: Organize and manage events.
- **Volunteer**: Participate in events.

### Running Tests
Use Maven to run tests:
```bash
mvn test
```

## Technical Notes
- **Security**: Implemented with Spring Security. Custom filters prevent actions by pending users.
- **Database Models**:
  - `User`: Stores user information, role, and status.
  - `Event`: Contains event details, organizer, and volunteers.
  - `Role`: Manages user roles (Admin, Organization, Volunteer).
  - `UserStatus` and `EventStatus`: Enum for tracking statuses.
- **Repositories**: Provided for User, Role, and Event with custom queries.

## Contributing
1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -m 'Add new feature'`
4. Push to the branch: `git push origin feature/new-feature`
5. Create a pull request.

