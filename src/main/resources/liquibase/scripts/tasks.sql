-- liquibase formatted sql

-- changeset tim405:1
CREATE TABLE notification_task (
    id SERIAL PRIMARY KEY,
    chat_id TEXT NOT NULL,
    notification TEXT NOT NULL,
    dateTime TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'no'
   );

  -- changeset tim405:2
  CREATE INDEX date_time_idx ON notification_task (dateTime);
