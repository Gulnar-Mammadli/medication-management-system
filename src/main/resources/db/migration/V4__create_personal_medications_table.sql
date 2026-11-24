CREATE TABLE personal_medications (
                                      id UUID PRIMARY KEY,
                                      user_id UUID NOT NULL,
                                      name VARCHAR(150) NOT NULL,
                                      dosage VARCHAR(100) NOT NULL,
                                      instructions VARCHAR(1000),
                                      start_date DATE NOT NULL,
                                      end_date DATE,
                                      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                      CONSTRAINT fk_personal_medications_user
                                          FOREIGN KEY (user_id)
                                              REFERENCES users(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT chk_personal_medication_status
                                          CHECK (status IN ('ACTIVE', 'COMPLETED', 'DISCONTINUED')),

                                      CONSTRAINT chk_personal_medication_dates
                                          CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_personal_medications_user_id
    ON personal_medications(user_id);
