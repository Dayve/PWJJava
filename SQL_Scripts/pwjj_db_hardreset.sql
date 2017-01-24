-- Delete every table, procedure and function in schema:
BEGIN
	FOR cur_rec IN (
		SELECT object_name, object_type
		FROM user_objects
		WHERE object_type IN (
			'TABLE',
			'PROCEDURE',
			'FUNCTION'
		)
	)
	LOOP
		BEGIN
			IF cur_rec.object_type = 'TABLE' THEN
				EXECUTE IMMEDIATE 'DROP '|| cur_rec.object_type || ' "' || cur_rec.object_name || '" CASCADE CONSTRAINTS';
			ELSE
				EXECUTE IMMEDIATE 'DROP ' || cur_rec.object_type || ' "' || cur_rec.object_name || '"';
			END IF;
			
			EXCEPTION
				WHEN OTHERS THEN
					DBMS_OUTPUT.put_line ('FAILED: DROP ' || cur_rec.object_type || ' "' || cur_rec.object_name || '"');
			END;
	END LOOP;
END;
/

-- Create tables:
CREATE TABLE ODPOWIEDZI (
	ID_ODP NUMBER(10) GENERATED BY DEFAULT ON NULL AS IDENTITY,
	TRESC VARCHAR2(50) NOT NULL,
	POPRAWNOSC NUMBER(1) NOT NULL,

	CONSTRAINT ODPOWIEDZI_PK PRIMARY KEY (ID_ODP)
);

CREATE TABLE KATEGORIE (
	ID_KAT NUMBER(6) GENERATED BY DEFAULT ON NULL AS IDENTITY,
	NAZWA_KAT VARCHAR2(40) NOT NULL,

	CONSTRAINT KATEGORIE_PK PRIMARY KEY (ID_KAT)
);

CREATE TABLE UZYTKOWNICY (
	ID_UZYT NUMBER(8) GENERATED BY DEFAULT ON NULL AS IDENTITY,
	LOGIN_UZYT VARCHAR2(20) NOT NULL,
	HASLO VARCHAR2(20) NOT NULL,
	IMIE VARCHAR2(30)  NOT NULL,
	NAZWISKO VARCHAR2(30)  NOT NULL,

	CONSTRAINT UZYTKOWNICY_PK PRIMARY KEY (ID_UZYT)
);

CREATE TABLE PYTANIA (
	ID_PYT NUMBER(8) GENERATED BY DEFAULT ON NULL AS IDENTITY,
	TRESC VARCHAR2(60) NOT NULL,
	ID_KAT NUMBER(6) CONSTRAINT FK_KATEGORIA_PYTANIA REFERENCES KATEGORIE(ID_KAT),

	CONSTRAINT PYTANIA_PK PRIMARY KEY (ID_PYT)
);

CREATE TABLE PYTANIE_ODPOWIEDZI (
	ID_PYT NUMBER(8) CONSTRAINT FK_ODPOWIEDZ_PYTANIE REFERENCES PYTANIA(ID_PYT),
	ID_ODP NUMBER(10) CONSTRAINT FK_PYTANIE_ODPOWIEDZ REFERENCES ODPOWIEDZI(ID_ODP)
);

CREATE TABLE TESTY (
	ID_TESTU NUMBER(6) GENERATED BY DEFAULT ON NULL AS IDENTITY,
	NAZWA_TESTU VARCHAR2(80) NOT NULL,
	OPIS_TESTU VARCHAR2(300) NOT NULL,
	CZAS_ROZPOCZECIA date NOT NULL,
	CZAS_ZAKONCZENIA date NOT NULL,
	ID_KAT NUMBER(6) CONSTRAINT FK_KATEGORIA_TESTU REFERENCES KATEGORIE(ID_KAT),

	CONSTRAINT TESTY_PK PRIMARY KEY (ID_TESTU)
);

CREATE TABLE POSTY (
    ID_POSTA NUMBER(8) GENERATED BY DEFAULT ON NULL AS IDENTITY,
    ID_TESTU NUMBER(6) CONSTRAINT FK_POST_TESTU REFERENCES TESTY(ID_TESTU),
    ID_UZYT NUMBER(8) CONSTRAINT FK_POST_UZYTKOWNIK REFERENCES UZYTKOWNICY(ID_UZYT),
    TRESC VARCHAR2(1000) NOT NULL,
    DATA_UTWORZENIA date NOT NULL,
	DATA_EDYCJI date NOT NULL,

    CONSTRAINT POSTY_PK PRIMARY KEY (ID_POSTA)
) ;

CREATE TABLE PYTANIA_TESTU (
	ID_PYT NUMBER(8) CONSTRAINT FK_TEST_PYTANIE REFERENCES PYTANIA(ID_PYT),
	ID_TESTU NUMBER(6) CONSTRAINT FK_ID_TESTU REFERENCES TESTY(ID_TESTU)
);

CREATE TABLE PODEJSCIA (
	ID_PODEJSCIA NUMBER(8) GENERATED BY DEFAULT ON NULL AS IDENTITY,
	WYNIK_TESTU NUMBER(3) NOT NULL, -- procent (liczba całkowita) całości możliwych punktów
	ID_TESTU NUMBER(6) CONSTRAINT FK_PODEJSCIE_DO_TESTU REFERENCES TESTY(ID_TESTU),
	ID_UZYT NUMBER(8) CONSTRAINT FK_UZYTKOWNIK_PODEJSCIE REFERENCES UZYTKOWNICY(ID_UZYT),

	CONSTRAINT PODEJSCIA_PK PRIMARY KEY (ID_PODEJSCIA)
);


