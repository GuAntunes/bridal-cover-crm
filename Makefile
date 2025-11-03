# Build & Install
install:
	./gradlew clean build

# Database
up:
	docker-compose up -d

down:
	docker-compose down

# Run application
run:
	./gradlew bootRun

# Tests
test:
	./gradlew test

# Build and verify
build:
	./gradlew build

# Clean build artifacts
clean:
	./gradlew clean

