CREATE TABLE ixsi."user"
(
  provider_id character varying,
  user_id character varying,
  state character varying,
  password character varying,
  pin integer,
  created timestamp without time zone,
  updated timestamp without time zone,
  CONSTRAINT "PK_user" PRIMARY KEY (provider_id, user_id),
  CONSTRAINT "FK_user_provider_id" FOREIGN KEY (provider_id)
    REFERENCES ixsi.provider (provider_id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);
