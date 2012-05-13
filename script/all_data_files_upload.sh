for f in ../data/*
do
     echo "Uploading data in $f"
     sh upload.sh $f
done
