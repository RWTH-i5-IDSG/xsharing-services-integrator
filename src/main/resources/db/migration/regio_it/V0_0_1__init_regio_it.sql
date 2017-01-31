CREATE TABLE regio_it.app_auth
(
  username character varying(255) NOT NULL,
  password character varying NOT NULL,
  tenant character varying(100) NOT NULL,
  token character varying,
  token_update_timestamp time with time zone,
  CONSTRAINT "PK_app_auth" PRIMARY KEY (username)
);
