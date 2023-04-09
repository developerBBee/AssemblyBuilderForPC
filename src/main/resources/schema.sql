CREATE TABLE IF NOT EXISTS systemvals (
  kakakuupdate TIMESTAMP
);
INSERT INTO systemvals
  SELECT * FROM (SELECT '2000-01-01 00:00:00')
    WHERE NOT EXISTS (SELECT * FROM systemvals)
;

CREATE TABLE IF NOT EXISTS devices (
  id VARCHAR(32) PRIMARY KEY,
  device VARCHAR(20),
  url VARCHAR(255),
  name VARCHAR(255),
  imgurl VARCHAR(255),
  detail VARCHAR(4095),
  price INT,
  rank INT,
  flag1 INT,
  flag2 INT,
  releasedate VARCHAR(8),
  invisible INT,
  createddate TIMESTAMP,
  lastupdate TIMESTAMP
);

-- User Management Database (guest only)
CREATE TABLE IF NOT EXISTS assemblies (
  id VARCHAR(32) PRIMARY KEY,
  deviceid VARCHAR(32),
  device VARCHAR(20),
  guestid VARCHAR(32),
  createddate TIMESTAMP,
  lastupdate TIMESTAMP
);

-- Save Function
CREATE TABLE IF NOT EXISTS savehead (
  saveid VARCHAR(32) PRIMARY KEY, -- save id (PrimaryKey and public URL)
  guestid VARCHAR(32), -- saved user guestId
  savename VARCHAR(255),
  createddate TIMESTAMP,
  lastupdate TIMESTAMP
);
CREATE TABLE IF NOT EXISTS savelist (
  saveid VARCHAR(32), -- save id (ForeignKey_CascadeDelete, Compositekey1)
  deviceid VARCHAR(32), -- saved deviceid (Compositekey2)
  price INT, -- saved price
  createddate TIMESTAMP,
  lastupdate TIMESTAMP,
  PRIMARY KEY(saveid, deviceid),
  FOREIGN KEY(saveid) REFERENCES savehead(saveid),
  FOREIGN KEY(deviceid) REFERENCES devices(id)
);

ALTER TABLE devices ADD COLUMN IF NOT EXISTS releasedate VARCHAR(8) DEFAULT '20000101';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS invisible INT DEFAULT 0;
ALTER TABLE devices ADD COLUMN IF NOT EXISTS createddate TIMESTAMP DEFAULT LOCALTIMESTAMP();
ALTER TABLE devices ADD COLUMN IF NOT EXISTS lastupdate TIMESTAMP DEFAULT LOCALTIMESTAMP();
ALTER TABLE assemblies ADD COLUMN IF NOT EXISTS createddate TIMESTAMP DEFAULT LOCALTIMESTAMP();
ALTER TABLE assemblies ADD COLUMN IF NOT EXISTS lastupdate TIMESTAMP DEFAULT LOCALTIMESTAMP();
