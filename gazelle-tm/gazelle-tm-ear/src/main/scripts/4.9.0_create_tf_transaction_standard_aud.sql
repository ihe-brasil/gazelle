-- to be executed if the audit table is not automatically created
CREATE TABLE tf_transaction_standard_aud
(
  transaction_id integer NOT NULL,
  revtype smallint,
  rev integer NOT NULL,
  standard_id integer NOT NULL,
  CONSTRAINT fk6302beab2fa28b00 FOREIGN KEY (transaction_id)
      REFERENCES tf_transaction (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk6302beab60fd5c54 FOREIGN KEY (standard_id)
      REFERENCES tf_standard (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT tf_transaction_standard_aud_standard_id_transaction_id_key UNIQUE (standard_id, transaction_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE tf_transaction_standard_aud
  OWNER TO gazelle;