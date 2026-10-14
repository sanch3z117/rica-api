CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    evento_id UUID NOT NULL UNIQUE,
    tipo VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    ocurrido_en TIMESTAMPTZ NOT NULL,
    publicado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_event_pendientes ON outbox_event (publicado, id);
