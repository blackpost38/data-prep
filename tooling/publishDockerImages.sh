#! /bin/bash

# called after a build. Do the following:
# - tags images with talend-registry
# - saves images as a tar
# - gzip this tar
# - upload tar.gz to ftp
# - push images to talend-registry

cd `dirname $0`

version=$1

if [ -z "$version"  ]; then
  echo "please specify a version"
  exit 1
fi

images='talend/dataprep-api talend/dataprep-dataset talend/dataprep-transformation talend/dataprep-preparation talend/dataprep-webapp'
registry=talend-registry:5000

FTP_HOST='ftp.talend.com'
FTP_USER='dataprep'
FTP_PASSWD='-_V 8bS){'


#===========================================
docker run -t mribeiro/figlet docker tag
#===========================================
for image in $images;
do
  completeName=$image:$version
  echo 'docker tag --force '$completeName $registry/$completeName
  docker tag --force $completeName $registry/$completeName
done
#===========================================


#===========================================
docker run -t mribeiro/figlet archive images
#===========================================
for image in $images;
do
  list+=$registry/$image:$version
  list+=' '
done
list+=$registry'/talend/dataprep-data:'$version' mongo:latest'

timestamp=`date +%Y%m%d%H%M%S`
tar_archive='dataprep-images_'$version'_'$timestamp'.tar'
docker pull $registry'/talend/dataprep-data:'$version
docker pull mongo:latest
echo 'docker save to '$tar_archive
time docker save --output=$tar_archive $list

echo 'gzip tar'
time gzip $tar_archive
#===========================================


#===========================================
docker run -t mribeiro/figlet FTP upload
#===========================================
tar_archive=$tar_archive'.gz'
md5sum $tar_archive > $tar_archive'.md5sum'

ftp -n $FTP_HOST <<END_SCRIPT
quote USER $FTP_USER
quote PASS $FTP_PASSWD
passive
cd dataprep/builds
put $tar_archive
put $tar_archive.md5sum
quit
END_SCRIPT
#===========================================


#===========================================
docker run -t mribeiro/figlet docker push
#===========================================
echo 'remove temp files'
rm $tar_archive*

for image in $images;
do
  completeName=$image:$version
  docker push $registry/$completeName
  docker rmi $registry/$completeName
done
#===========================================


#===========================================
docker run -t mribeiro/figlet notify dev server
#===========================================
ssh talend@dev.data-prep.talend.lan 'bash -s' < notifyRemote.sh $version
#===========================================

