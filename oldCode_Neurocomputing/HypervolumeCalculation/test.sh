#Erase old files
if [ -f "hypervolumes.txt" ]
then
	rm hypervolumes.txt
fi
#Execute for all tests
for i in $(seq 10 10 390)
do
	#Execute the hypervolume calculation
	OUTPUT=$(./wfg "$i".txt 0 0 0)
	#Saves the results in variables
	DWL=$(expr substr "$OUTPUT" 9 12)
	SSBW=$(expr substr "$OUTPUT" 49 12)

	#Output result to file
	echo "$i $DWL $SSBW" >> hypervolumes.txt
done


