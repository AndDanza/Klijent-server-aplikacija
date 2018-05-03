CREATE TABLE uredaji (
  id int NOT NULL DEFAULT 1,
  naziv varchar(30) NOT NULL DEFAULT '',
  sadrzaj varchar(500) NOT NULL DEFAULT '',
  vrijeme_promjene timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  
  vrijeme_kreiranja timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE dnevnik (
  id int NOT NULL DEFAULT 1,
  sadrzaj varchar(500) NOT NULL DEFAULT '',
  vrijeme timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

insert into uredaji (id, naziv, sadrzaj) values (1, 'Senzor temperature', '{"id": 1, "komanda": "azuriraj", "naziv": "Senzor temperature", "temp": 20.4, "vrijeme": "2018.04.08 11:20:45"}');
insert into uredaji (id, naziv, sadrzaj) values (2, 'Senzor RFID', '{"id": 2, "komanda": "azuriraj", "naziv": "Senzor RFID", "korisnik": "mato", "vrijeme": "2018.04.09 21:09:01"}');
insert into uredaji (id, naziv, sadrzaj) values (3, 'Meteo stanica', '{"id": 3, "komanda": "dodaj", "naziv": "Meteo stanica", "temp": 22.0, "vlaga": 77, "vjetar": "NW"  "vrijeme": "2018.04.09 18:19:41"}');


insert into dnevnik (id, sadrzaj) values (1, '{"id": 1, "komanda": "dodaj", "naziv": "Senzor temperature", "vrijeme": "2018.04.08 11:20:45"}');
insert into dnevnik (id, sadrzaj) values (1, '{"id": 1, "komanda": "azuriraj", "temp": 20.4, "vrijeme": "2018.04.08 11:26:10"}');
insert into dnevnik (id, sadrzaj) values (1, '{"id": 2, "komanda": "dodaj", "naziv": "Senzor RFID", "korisnik": "pero", "vrijeme": "2018.04.09 17:21:56"}');
insert into dnevnik (id, sadrzaj) values (1, '{"id": 2, "komanda": "azuriraj", "korisnik": "mato", "vrijeme": "2018.04.09 21:09:01"}');
insert into dnevnik (id, sadrzaj) values (1, '{"id": 3, "komanda": "dodaj", "naziv": "Meteo stanica", "temp": 22.0, "vlaga": 77, "vjetar": "NW"  "vrijeme": "2018.04.09 18:19:41"}');

