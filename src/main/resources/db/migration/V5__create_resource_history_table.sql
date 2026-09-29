CREATE TABLE IF NOT EXISTS resource_history (
    id VARCHAR(36) PRIMARY KEY,
    resource_id VARCHAR(36) NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    previous_value TEXT,
    new_value TEXT,
    resource_version BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_resource_history_resource_id ON resource_history(resource_id);
CREATE INDEX idx_resource_history_changed_by ON resource_history(changed_by);
CREATE INDEX idx_resource_history_created_at ON resource_history(created_at);