-- to be executed if the audit table is not automatically created
CREATE TABLE tf_standard_aud
(
  id integer NOT NULL,
  last_changed timestamp without time zone,
  last_modifier_id character varying(255),
  revtype smallint,
  rev integer NOT NULL,
  keyword character varying(255),
  name text,
  network_communication_type integer,
  url text,
  version character varying(255),
  CONSTRAINT tf_standard_aud_pkey PRIMARY KEY (id),
  CONSTRAINT tf_standard_aud_keyword_key UNIQUE (keyword)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE tf_standard_aud
  OWNER TO gazelle;