/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2022 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

CREATE SEQUENCE ES_DOCUMENT_RESULT_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 10 START WITH 1 CACHE 20;

CREATE TABLE ES_ONBOARDING_PROCESS (
    ID VARCHAR2(36 CHAR) NOT NULL PRIMARY KEY,
    IDENTIFICATION_DATA VARCHAR2(1024 CHAR) NOT NULL,
    USER_ID VARCHAR2(256 CHAR),
    ACTIVATION_ID VARCHAR2(36 CHAR),
    STATUS VARCHAR2(32 CHAR) NOT NULL,
    ACTIVATION_REMOVED NUMBER(1) DEFAULT 0,
    ERROR_DETAIL VARCHAR2(256 CHAR),
    ERROR_ORIGIN VARCHAR2(256 CHAR),
    ERROR_SCORE INTEGER DEFAULT 0 NOT NULL,
    TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
    TIMESTAMP_LAST_UPDATED TIMESTAMP(6),
    TIMESTAMP_FINISHED TIMESTAMP(6),
    TIMESTAMP_FAILED TIMESTAMP(6)
);

CREATE INDEX ONBOARDING_PROCESS_STATUS ON ES_ONBOARDING_PROCESS (STATUS);
CREATE INDEX ONBOARDING_PROCESS_IDENTIF_DATA ON ES_ONBOARDING_PROCESS (IDENTIFICATION_DATA);
CREATE INDEX ONBOARDING_PROCESS_TIMESTAMP_1 ON ES_ONBOARDING_PROCESS (TIMESTAMP_CREATED);
CREATE INDEX ONBOARDING_PROCESS_TIMESTAMP_2 ON ES_ONBOARDING_PROCESS (TIMESTAMP_LAST_UPDATED);

CREATE TABLE ES_ONBOARDING_OTP (
    ID VARCHAR2(36 CHAR) NOT NULL PRIMARY KEY,
    PROCESS_ID VARCHAR2(36 CHAR) NOT NULL,
    OTP_CODE VARCHAR2(32 CHAR) NOT NULL,
    STATUS VARCHAR2(32 CHAR) NOT NULL,
    TYPE VARCHAR2(32 CHAR) NOT NULL,
    ERROR_DETAIL VARCHAR2(256 CHAR),
    ERROR_ORIGIN VARCHAR2(256 CHAR),
    FAILED_ATTEMPTS INTEGER,
    TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
    TIMESTAMP_EXPIRATION TIMESTAMP(6) NOT NULL,
    TIMESTAMP_LAST_UPDATED TIMESTAMP(6),
    TIMESTAMP_VERIFIED TIMESTAMP(6),
    TIMESTAMP_FAILED TIMESTAMP(6),
    FOREIGN KEY (PROCESS_ID) REFERENCES ES_ONBOARDING_PROCESS (ID)
);

-- Oracle does not create indexes on foreign keys automatically
CREATE INDEX ONBOARDING_PROCESS ON ES_ONBOARDING_OTP (PROCESS_ID);
CREATE INDEX ONBOARDING_OTP_STATUS ON ES_ONBOARDING_OTP (STATUS);
CREATE INDEX ONBOARDING_OTP_TIMESTAMP_1 ON ES_ONBOARDING_OTP (TIMESTAMP_CREATED);
CREATE INDEX ONBOARDING_OTP_TIMESTAMP_2 ON ES_ONBOARDING_OTP (TIMESTAMP_LAST_UPDATED);

CREATE TABLE ES_IDENTITY_VERIFICATION (
    ID VARCHAR2(36 CHAR) NOT NULL PRIMARY KEY,
    ACTIVATION_ID VARCHAR2(36 CHAR) NOT NULL,
    USER_ID VARCHAR2(256 CHAR) NOT NULL,
    PROCESS_ID VARCHAR2(36 CHAR) NOT NULL,
    STATUS VARCHAR2(32 CHAR) NOT NULL,
    PHASE VARCHAR2(32 CHAR) NOT NULL,
    REJECT_REASON CLOB,
    REJECT_ORIGIN VARCHAR2(256 CHAR),
    ERROR_DETAIL VARCHAR2(256 CHAR),
    ERROR_ORIGIN VARCHAR2(256 CHAR),
    SESSION_INFO CLOB,
    TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
    TIMESTAMP_LAST_UPDATED TIMESTAMP(6),
    TIMESTAMP_FINISHED TIMESTAMP(6),
    TIMESTAMP_FAILED TIMESTAMP(6),
    FOREIGN KEY (PROCESS_ID) REFERENCES ES_ONBOARDING_PROCESS (ID)
);

CREATE INDEX IDENTITY_VERIF_ACTIVATION ON ES_IDENTITY_VERIFICATION (ACTIVATION_ID);
CREATE INDEX IDENTITY_VERIF_USER ON ES_IDENTITY_VERIFICATION (USER_ID);
CREATE INDEX IDENTITY_VERIF_STATUS ON ES_IDENTITY_VERIFICATION (STATUS);
CREATE INDEX IDENTITY_VERIF_PHASE ON ES_IDENTITY_VERIFICATION (PHASE);
CREATE INDEX IDENTITY_VERIF_TIMESTAMP_1 ON ES_IDENTITY_VERIFICATION (TIMESTAMP_CREATED);
CREATE INDEX IDENTITY_VERIF_TIMESTAMP_2 ON ES_IDENTITY_VERIFICATION (TIMESTAMP_LAST_UPDATED);

CREATE TABLE ES_DOCUMENT_VERIFICATION (
    ID VARCHAR2(36 CHAR) NOT NULL PRIMARY KEY,
    ACTIVATION_ID VARCHAR2(36 CHAR) NOT NULL,
    IDENTITY_VERIFICATION_ID VARCHAR2(36 CHAR) NOT NULL,
    TYPE VARCHAR2(32 CHAR) NOT NULL,
    SIDE VARCHAR2(5 CHAR),
    OTHER_SIDE_ID VARCHAR2(36 CHAR),
    PROVIDER_NAME VARCHAR2(64 CHAR),
    STATUS VARCHAR2(32 CHAR) NOT NULL,
    FILENAME VARCHAR2(256 CHAR) NOT NULL,
    UPLOAD_ID VARCHAR2(36 CHAR),
    VERIFICATION_ID VARCHAR2(36 CHAR),
    PHOTO_ID VARCHAR2(256 CHAR),
    VERIFICATION_SCORE INTEGER,
    REJECT_REASON CLOB,
    REJECT_ORIGIN VARCHAR2(256 CHAR),
    ERROR_DETAIL VARCHAR2(256 CHAR),
    ERROR_ORIGIN VARCHAR2(256 CHAR),
    ORIGINAL_DOCUMENT_ID VARCHAR2(36 CHAR),
    USED_FOR_VERIFICATION NUMBER(1) DEFAULT 0,
    TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
    TIMESTAMP_UPLOADED TIMESTAMP(6),
    TIMESTAMP_VERIFIED TIMESTAMP(6),
    TIMESTAMP_DISPOSED TIMESTAMP(6),
    TIMESTAMP_LAST_UPDATED TIMESTAMP(6),
    FOREIGN KEY (IDENTITY_VERIFICATION_ID) REFERENCES ES_IDENTITY_VERIFICATION (ID)
);

-- Oracle does not create indexes on foreign keys automatically
CREATE INDEX DOCUMENT_IDENT_VERIF ON ES_DOCUMENT_VERIFICATION (IDENTITY_VERIFICATION_ID);
CREATE INDEX DOCUMENT_VERIF_ACTIVATION ON ES_DOCUMENT_VERIFICATION (ACTIVATION_ID);
CREATE INDEX DOCUMENT_VERIF_STATUS ON ES_DOCUMENT_VERIFICATION (STATUS);
CREATE INDEX DOCUMENT_VERIF_TIMESTAMP_1 ON ES_DOCUMENT_VERIFICATION (TIMESTAMP_CREATED);
CREATE INDEX DOCUMENT_VERIF_TIMESTAMP_2 ON ES_DOCUMENT_VERIFICATION (TIMESTAMP_LAST_UPDATED);

CREATE TABLE ES_DOCUMENT_DATA (
    ID VARCHAR2(36 CHAR) NOT NULL PRIMARY KEY,
    ACTIVATION_ID VARCHAR2(36 CHAR) NOT NULL,
    IDENTITY_VERIFICATION_ID VARCHAR2(36 CHAR) NOT NULL,
    FILENAME VARCHAR2(256 CHAR) NOT NULL,
    DATA BLOB NOT NULL,
    TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (IDENTITY_VERIFICATION_ID) REFERENCES ES_IDENTITY_VERIFICATION (ID)
);

CREATE INDEX DOCUMENT_DATA_ACTIVATION ON ES_DOCUMENT_DATA (ACTIVATION_ID);
CREATE INDEX DOCUMENT_DATA_TIMESTAMP ON ES_DOCUMENT_DATA (TIMESTAMP_CREATED);

CREATE TABLE ES_DOCUMENT_RESULT (
    ID NUMBER(19) NOT NULL PRIMARY KEY,
    DOCUMENT_VERIFICATION_ID VARCHAR2(36 CHAR) NOT NULL,
    PHASE VARCHAR2(32 CHAR) NOT NULL,
    REJECT_REASON CLOB,
    REJECT_ORIGIN VARCHAR2(256 CHAR),
    VERIFICATION_RESULT CLOB,
    ERROR_DETAIL CLOB,
    ERROR_ORIGIN VARCHAR2(256 CHAR),
    EXTRACTED_DATA CLOB,
    TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (DOCUMENT_VERIFICATION_ID) REFERENCES ES_DOCUMENT_VERIFICATION (ID)
);

-- Oracle does not create indexes on foreign keys automatically
CREATE INDEX DOCUMENT_VERIF_RESULT ON ES_DOCUMENT_RESULT (DOCUMENT_VERIFICATION_ID);

-- Scheduler lock table - https://github.com/lukas-krecan/ShedLock#configure-lockprovider
BEGIN EXECUTE IMMEDIATE 'CREATE TABLE shedlock (
    name VARCHAR2(64 CHAR) NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL,
    locked_by VARCHAR2(255 CHAR) NOT NULL,
    PRIMARY KEY (name))';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

-- Create audit log table - https://github.com/wultra/lime-java-core#wultra-auditing-library
BEGIN EXECUTE IMMEDIATE 'CREATE TABLE audit_log (
    audit_log_id       VARCHAR2(36 CHAR) PRIMARY KEY,
    application_name   VARCHAR2(256 CHAR) NOT NULL,
    audit_level        VARCHAR2(32 CHAR) NOT NULL,
    audit_type         VARCHAR2(256 CHAR),
    timestamp_created  TIMESTAMP,
    message            CLOB NOT NULL,
    exception_message  CLOB,
    stack_trace        CLOB,
    param              CLOB,
    calling_class      VARCHAR2(256 CHAR) NOT NULL,
    thread_name        VARCHAR2(256 CHAR) NOT NULL,
    version            VARCHAR2(256 CHAR),
    build_time         TIMESTAMP)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE TABLE audit_param (
    audit_log_id       VARCHAR2(36 CHAR),
    timestamp_created  TIMESTAMP,
    param_key          VARCHAR2(256 CHAR),
    param_value        VARCHAR2(4000 CHAR))';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_log_timestamp ON audit_log (timestamp_created)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_log_application ON audit_log (application_name)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_log_level ON audit_log (audit_level)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_log_type ON audit_log (audit_type)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_param_log ON audit_param (audit_log_id)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_param_timestamp ON audit_param (timestamp_created)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_param_key ON audit_param (param_key)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_param_value ON audit_param (param_value)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
