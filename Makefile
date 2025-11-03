# Build & Install
install:
	./gradlew clean build

# Database
up:
	docker-compose up -d

down:
	docker-compose down

# Database only
db-up:
	docker-compose up -d postgres pgadmin

db-down:
	docker-compose stop postgres pgadmin

# Jenkins
jenkins-up:
	docker-compose up -d jenkins
	@echo "Jenkins is starting..."
	@echo "Access Jenkins at: http://localhost:9090"
	@echo "Getting initial admin password (wait 30 seconds for Jenkins to start)..."
	@sleep 30
	@docker exec bridal-cover-crm-jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo "Jenkins still starting, please wait and run: make jenkins-password"

jenkins-down:
	docker-compose stop jenkins

jenkins-password:
	@echo "Jenkins Initial Admin Password:"
	@docker exec bridal-cover-crm-jenkins cat /var/jenkins_home/secrets/initialAdminPassword

jenkins-logs:
	docker logs -f bridal-cover-crm-jenkins

jenkins-restart:
	docker-compose restart jenkins

# Run application
run:
	./gradlew bootRun

# Tests
test:
	./gradlew test

arch-test:
	./gradlew test --tests "*ArchitectureTest"

# Build and verify
build:
	./gradlew build

# Clean build artifacts
clean:
	./gradlew clean

# Full environment
start-all:
	docker-compose up -d
	@echo "All services started!"
	@echo "- PostgreSQL: localhost:5432"
	@echo "- PgAdmin: http://localhost:8081"
	@echo "- Jenkins: http://localhost:9090"

stop-all:
	docker-compose down

# Help
help:
	@echo "Available commands:"
	@echo "  make install       - Build and install dependencies"
	@echo "  make up            - Start all services"
	@echo "  make down          - Stop all services"
	@echo "  make db-up         - Start only database services"
	@echo "  make db-down       - Stop database services"
	@echo "  make jenkins-up    - Start Jenkins"
	@echo "  make jenkins-down  - Stop Jenkins"
	@echo "  make jenkins-password - Get Jenkins initial password"
	@echo "  make jenkins-logs  - View Jenkins logs"
	@echo "  make run           - Run the application"
	@echo "  make test          - Run tests"
	@echo "  make arch-test     - Run architecture tests"
	@echo "  make build         - Build the application"
	@echo "  make clean         - Clean build artifacts"
	@echo "  make start-all     - Start all services"
	@echo "  make stop-all      - Stop all services"

