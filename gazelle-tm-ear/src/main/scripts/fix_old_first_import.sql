--
-- Name: tf_document_aud; Type: TABLE; Schema: public; Owner: gazelle; Tablespace: 
--

CREATE TABLE tf_document_aud (
    id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    last_changed timestamp without time zone,
    last_modifier_id character varying(255),
    document_dateofpublication timestamp without time zone,
    md5_hash_code character varying(255),
    document_lifecyclestatus integer,
    document_linkstatus integer,
    document_name character varying(255),
    document_revision character varying(255),
    document_title character varying(255),
    document_type integer,
    document_url character varying(255),
    document_volume character varying(255),
    document_domain_id integer,
    document_linkstatusdescription character varying(255)
);


ALTER TABLE public.tf_document_aud OWNER TO gazelle;

--
-- Name: tf_document_sections_aud; Type: TABLE; Schema: public; Owner: gazelle; Tablespace: 
--

CREATE TABLE tf_document_sections_aud (
    id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    last_changed timestamp without time zone,
    last_modifier_id character varying(255),
    section character varying(255),
    type integer,
    document_id integer
);


ALTER TABLE public.tf_document_sections_aud OWNER TO gazelle;

--
-- Name: tm_jira_issues_to_test_aud; Type: TABLE; Schema: public; Owner: gazelle; Tablespace: 
--

CREATE TABLE tm_jira_issues_to_test_aud (
    id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    jira_key character varying(255),
    jira_status character varying(255),
    jira_summary character varying(255),
    test_id integer
);


ALTER TABLE public.tm_jira_issues_to_test_aud OWNER TO gazelle;

--
-- Name: usr_gazelle_language_aud; Type: TABLE; Schema: public; Owner: gazelle; Tablespace: 
--

CREATE TABLE usr_gazelle_language_aud (
    id integer NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    last_changed timestamp without time zone,
    last_modifier_id character varying(255),
    description character varying(255),
    keyword character varying(255)
);


ALTER TABLE public.usr_gazelle_language_aud OWNER TO gazelle;

--
-- Name: tf_document_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: gazelle; Tablespace: 
--

ALTER TABLE ONLY tf_document_aud
    ADD CONSTRAINT tf_document_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: tf_document_sections_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: gazelle; Tablespace: 
--

ALTER TABLE ONLY tf_document_sections_aud
    ADD CONSTRAINT tf_document_sections_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: tm_jira_issues_to_test_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: gazelle; Tablespace: 
--

ALTER TABLE ONLY tm_jira_issues_to_test_aud
    ADD CONSTRAINT tm_jira_issues_to_test_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: usr_gazelle_language_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: gazelle; Tablespace: 
--

ALTER TABLE ONLY usr_gazelle_language_aud
    ADD CONSTRAINT usr_gazelle_language_aud_pkey PRIMARY KEY (id, rev);
    
    --
-- Name: fk5cb04f96df74e053; Type: FK CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY tf_document_sections_aud
    ADD CONSTRAINT fk5cb04f96df74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: fkbe6eed0ddf74e053; Type: FK CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY tm_jira_issues_to_test_aud
    ADD CONSTRAINT fkbe6eed0ddf74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: fke09ede19df74e053; Type: FK CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY tf_document_aud
    ADD CONSTRAINT fke09ede19df74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: fkea14b593df74e053; Type: FK CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY usr_gazelle_language_aud
    ADD CONSTRAINT fkea14b593df74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


alter table tm_test_aud add column validated boolean;

alter table tf_transaction_aud add column documentsection integer;

alter table tf_actor_integration_profile_option_aud add column maybe_supportive boolean;

alter table tf_actor_integration_profile_option_aud add column document_section integer;

alter table tf_integration_profile_aud add column documentsection integer;

ALTER TABLE tf_integration_profile_option ALTER COLUMN description TYPE character varying(2048);

ALTER TABLE tf_integration_profile_option_AUD ALTER COLUMN description TYPE character varying(2048);


