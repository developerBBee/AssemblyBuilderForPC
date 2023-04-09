#!/bin/bash -eu
sudo cp -rf /var/pcassem/pcassem-0.0.1-SNAPSHOT.jar /home/ubuntu/pcassem-0.0.1-SNAPSHOT.jar_bk
sudo chown ubuntu:ubuntu /home/ubuntu/pcassem-0.0.1-SNAPSHOT.jar_bk
echo "jar file backup."
sudo cp -rf /home/ubuntu/db/kakakudb.mv.db /home/ubuntu/kakakudb.mv.db_bk
sudo cp -rf /home/ubuntu/db/kakakudb.trace.db /home/ubuntu/kakakudb.trace.db_bk
sudo chown ubuntu:ubuntu /home/ubuntu/kakakudb.mv.db_bk
sudo chown ubuntu:ubuntu /home/ubuntu/kakakudb.trace.db_bk
echo "db file backup."
sudo systemctl stop pcassem.service
echo "service stopped."
sudo cp -rf /home/ubuntu/app/pcassem-0.0.1-SNAPSHOT.jar /var/pcassem/pcassem-0.0.1-SNAPSHOT.jar
echo "jar file copied."
sudo chmod 777 /var/pcassem/pcassem-0.0.1-SNAPSHOT.jar
echo "jar file mode changed."
sudo systemctl start pcassem.service
echo "service started."
echo "script completed."