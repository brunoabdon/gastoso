--
-- Name: conta; Type: TABLE; Schema: public; Tablespace: 
--

CREATE TABLE conta (
    id integer NOT NULL,
    nome character varying(50) NOT NULL
);

--
-- Name: conta_id_seq; Type: SEQUENCE; Schema: public; 
--

CREATE SEQUENCE conta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE conta_id_seq OWNED BY conta.id;

--
-- Name: fato; Type: TABLE; Schema: public; Tablespace: 
--

CREATE TABLE fato (
    id integer NOT NULL,
    descricao character varying(70) NOT NULL,
    dia date NOT NULL
);

--
-- Name: fato_id_seq; Type: SEQUENCE; Schema: public; 
--

CREATE SEQUENCE fato_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: fato_id_seq; Type: SEQUENCE OWNED BY; Schema: public; 
--

ALTER SEQUENCE fato_id_seq OWNED BY fato.id;

--
-- Name: lancamento; Type: TABLE; Schema: public; Tablespace: 
--

CREATE TABLE lancamento (
    id integer NOT NULL,
    valor integer NOT NULL,
    conta_id integer NOT NULL,
    fato_id integer NOT NULL
);


--
-- Name: lancamento_id_seq; Type: SEQUENCE; Schema: public; 
--

CREATE SEQUENCE lancamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: lancamento_id_seq; Type: SEQUENCE OWNED BY; Schema: public; 
--

ALTER SEQUENCE lancamento_id_seq OWNED BY lancamento.id;

--
-- Name: id; Type: DEFAULT; Schema: public; 
--

ALTER TABLE ONLY conta ALTER COLUMN id SET DEFAULT nextval('conta_id_seq'::regclass);

--
-- Name: id; Type: DEFAULT; Schema: public; 
--

ALTER TABLE ONLY fato ALTER COLUMN id SET DEFAULT nextval('fato_id_seq'::regclass);

--
-- Name: id; Type: DEFAULT; Schema: public; 
--

ALTER TABLE ONLY lancamento ALTER COLUMN id SET DEFAULT nextval('lancamento_id_seq'::regclass);

--
-- Name: conta_pkey; Type: CONSTRAINT; Schema: public; Tablespace: 
--

ALTER TABLE ONLY conta
    ADD CONSTRAINT conta_pkey PRIMARY KEY (id);

--
-- Name: fato_pkey; Type: CONSTRAINT; Schema: public; Tablespace: 
--

ALTER TABLE ONLY fato
    ADD CONSTRAINT fato_pkey PRIMARY KEY (id);

--
-- Name: lancamento_conta_id_fato_id_key; Type: CONSTRAINT; Schema: public; Tablespace: 
--

ALTER TABLE ONLY lancamento
    ADD CONSTRAINT lancamento_conta_id_fato_id_key UNIQUE (conta_id, fato_id);

--
-- Name: lancamento_pkey; Type: CONSTRAINT; Schema: public; Tablespace: 
--

ALTER TABLE ONLY lancamento
    ADD CONSTRAINT lancamento_pkey PRIMARY KEY (id);


--
-- Name: uk_e13vpdjl92vm6c1wbv437jptw; Type: CONSTRAINT; Schema: public; Tablespace: 
--

ALTER TABLE ONLY conta
    ADD CONSTRAINT uk_conta_nome UNIQUE (nome);

--
-- Name: fk_c2b324si6jjtbwjx5uuapnbxf; Type: FK CONSTRAINT; Schema: public; 
--

ALTER TABLE ONLY lancamento
    ADD CONSTRAINT fk_lancamento_fato FOREIGN KEY (fato_id) REFERENCES fato(id);

--
-- Name: fk_petxqfcufqb2nbsi5vht1fiu9; Type: FK CONSTRAINT; Schema: public; 
--

ALTER TABLE ONLY lancamento
    ADD CONSTRAINT fk_lacamento_conta FOREIGN KEY (conta_id) REFERENCES conta(id);