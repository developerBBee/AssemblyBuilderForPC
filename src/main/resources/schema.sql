ALTER TABLE devices ADD COLUMN IF NOT EXISTS flag1 INT DEFAULT 0;
ALTER TABLE devices ADD COLUMN IF NOT EXISTS flag2 INT DEFAULT 0;

CREATE TABLE IF NOT EXISTS devices (
  id VARCHAR(32) PRIMARY KEY,
  device VARCHAR(20),
  url VARCHAR(100),
  name VARCHAR(100),
  imgurl VARCHAR(255),
  detail VARCHAR(4095),
  price INT,
  rank INT,
  flag1 INT,
  flag2 INT
);

-- User Management Database (guest only)
CREATE TABLE IF NOT EXISTS assemblies (
  id VARCHAR(32) PRIMARY KEY,
  deviceid VARCHAR(32),
  device VARCHAR(20),
  guestid VARCHAR(32)
);