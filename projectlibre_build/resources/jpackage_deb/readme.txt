To create a deb, run:
ant clean
ant jpackage-deb
cd packages/jpackage-deb
./make.sh

Note that you need JDK 26 and fakeroot to build the deb:
sudo apt install openjdk-26-jdk
sudo apt install fakeroot

To create a rpm:
sudo apt install alien
sudo alien -r app/projectlibre_<the current version>_<your arch>.deb
