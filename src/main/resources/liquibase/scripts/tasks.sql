-- liquibase formatted sql

-- changeset tim405:1
CREATE TABLE notification_tasks (
    id SERIAL PRIMARY KEY,
    chat_id TEXT NOT NULL,
    notification TEXT NOT NULL,
    dateTime TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'pending'
   );