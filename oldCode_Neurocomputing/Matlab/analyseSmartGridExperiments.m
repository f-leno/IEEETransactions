function analyseSmartGridExperiments(folder,initTrial,endTrial)
%analyseSmartGridExperiments This function opens .csv files generated in Burlap
%and creates all graphs used in the paper
%   folder: Path for folder with files
%   initTrial: first trial to be opened (usually 1)
%   endTrial: last experiment Trial

    cd(folder);
    close('all');
    infoDir = dir('.');
    
    algorithmNames = cell(0,1);
    numAlg = 0;
    
    %Read all folders in the given dir
    for i=1:size(infoDir,1)
       folderName = infoDir(i).name;
       
       %Only takes into account folders
       if(~strcmp(folderName,'.') && ~strcmp(folderName,'..') && infoDir(1).isdir )
           numAlg = numAlg+1;
           algorithmNames{numAlg} = folderName;           
           
       end       
    end
    
    trials = cell(1,length(initTrial:endTrial));
    algValues = cell(1,numAlg);
    %Algorithm - Trials - Days of Training
    for i=1:numAlg
        %access the folder
        cd(algorithmNames{i})
        for trial = initTrial:endTrial
            [trials{trial}] = getAlgMetrics(algorithmNames{i},trial); 
        end
        algValues{i} = trials;
        cd('..');
        %Return to previous folder
    end
    
    printGraphs(algValues,algorithmNames);
    
end


function printGraphs(algValues,algorithmNames)
%printGraphs  Print all relevant Graphs
% algValues - cell(1,nAlg) -> cell(1,nTrials) -> cell(nValues,2) (1 =
% value, 2=) -> (lines,1) -> Each attribute(matrix)

%printRewardGraphs(algValues,algorithmNames);
%printNumberOfDiscEv(algValues,algorithmNames);
%printTravelReference(algValues);
%generateFrontFiles(algValues,algorithmNames);


end




function generateFrontFiles(algValues,algorithmNames)
%Generate a file to compare hyperVolume Metrics

[metrics,xValues] = getProcessedRewardMetrics(algValues,length(algorithmNames));

dwlPoints = zeros(0,size(metrics,3),length(xValues),3);

ssbwPoints = zeros(0,size(metrics,3),length(xValues),3);

for i=1:length(algorithmNames)
    if ~isempty(strfind(algorithmNames{i},'DWL'))
        index = size(dwlPoints,1)+1;
        for y=1:length(xValues)            
            for trial=1:size(metrics,3)
               dwlPoints(index,trial,y,1) =  metrics{i,1,trial,y,2};
               dwlPoints(index,trial,y,2) =  metrics{i,2,trial,y,2};
               dwlPoints(index,trial,y,3) =  metrics{i,3,trial,y,2};
            end
        end
    end
    if ~isempty(strfind(algorithmNames{i},'SSBW'))
        index = size(ssbwPoints,1)+1;
        for y=1:length(xValues)            
            for trial=1:size(metrics,3)
               ssbwPoints(index,trial,y,1) =  metrics{i,1,trial,y,2};
               ssbwPoints(index,trial,y,2) =  metrics{i,2,trial,y,2};
               ssbwPoints(index,trial,y,3) =  metrics{i,3,trial,y,2};
            end
        end
    end
end

%Process average value
averPointsDWL = zeros(size(dwlPoints,1),size(dwlPoints,3),size(dwlPoints,4));
averPointsSSBW = zeros(size(ssbwPoints,1),size(ssbwPoints,3),size(ssbwPoints,4));
averPointsDWL(:,:,:) = mean(dwlPoints,2);
averPointsSSBW(:,:,:) = mean(ssbwPoints,2);

maxValue1 = max([max(averPointsDWL(:,:,1)) max(averPointsSSBW(:,:,1))]);
maxValue2 = max([max(averPointsDWL(:,:,2)) max(averPointsSSBW(:,:,2))]);
maxValue3 = max([max(averPointsDWL(:,:,3)) max(averPointsSSBW(:,:,3))]);
for xValue=1:length(xValues)
    fileID = fopen([num2str(xValues(xValue)),'.txt'],'w');
    fprintf(fileID,'#\n');
    nonDominatedDWL = zeros(0,3);
    nonDominatedSSBW = zeros(0,3);
    for i = 1:size(averPointsDWL,1)
        curPoint = averPointsDWL(i,xValue,:);
        include = true;
        index = 1;
        while include && index<=size(nonDominatedDWL,1)
           if(curPoint(1) <= nonDominatedDWL(index,1) && curPoint(2) <= nonDominatedDWL(index,2) && curPoint(3) <= nonDominatedDWL(index,3))
               include = false;
               index = index+1;
           else
               if(curPoint(1) >= nonDominatedDWL(index,1) && curPoint(2) >= nonDominatedDWL(index,2) && curPoint(3) >= nonDominatedDWL(index,3))
                     nonDominatedDWL(index,:) = [];
               else
                    index = index+1;
               end
           end
            
        end
        if(include)
            nonDominatedDWL(end+1,:) = curPoint;
        end

    end
    nonDominatedDWL(:,1) = nonDominatedDWL(:,1) / maxValue1;
    nonDominatedDWL(:,2) = nonDominatedDWL(:,2) / maxValue2;
    nonDominatedDWL(:,3) = nonDominatedDWL(:,3) / maxValue3;
    for a=1:size(nonDominatedDWL,1)
        fprintf(fileID,[num2str(nonDominatedDWL(a,:), '%15.4f'),'\n']);
    end
    fprintf(fileID,'#\n');
    for i = 1:size(averPointsSSBW,1)
        curPoint = averPointsSSBW(i,xValue,:);
        include = true;
        index = 1;
        while include && index<=size(nonDominatedSSBW,1)
           if(curPoint(1) <= nonDominatedSSBW(index,1) && curPoint(2) <= nonDominatedSSBW(index,2) && curPoint(3) <= nonDominatedSSBW(index,3))
               include = false;
               index = index+1;
           else
               if(curPoint(1) >= nonDominatedSSBW(index,1) && curPoint(2) >= nonDominatedSSBW(index,2) && curPoint(3) >= nonDominatedSSBW(index,3))
                     nonDominatedSSBW(index,:) = [];
               else
                    index = index+1;
               end
           end
            
        end
        if(include)
            nonDominatedSSBW(end+1,:) = curPoint;
        end

    end
    nonDominatedSSBW(:,1) = nonDominatedSSBW(:,1) / maxValue1;
    nonDominatedSSBW(:,2) = nonDominatedSSBW(:,2) / maxValue2;
    nonDominatedSSBW(:,3) = nonDominatedSSBW(:,3) / maxValue3;
    for a=1:size(nonDominatedSSBW,1)
        fprintf(fileID,[num2str(nonDominatedSSBW(a,:), '%15.4f'),'\n']);
    end
    fprintf(fileID,'#');
    fclose(fileID);
    
end
end


function printTravelReference(algValues)
%This function prints the reference to EVs traveling
    evsAtHome = getProcessedTravelCounts(algValues);
    xValues = 0:0.25:23.75;
    figure;
    handlePlot = plot(xValues,evsAtHome);
    set(handlePlot,'LineWidth',3);
    

end

function numHome = getProcessedTravelCounts(algValues)
%Counts the number of evs at home for each time step
lines = cell2mat(algValues{1}{1}{1,2});
i = 1;
numHome = zeros(0,1);
while lines(i,1)==1
   numHome(i) = lines(i,7); %AtHome attribute is in this position
   i = i+1; 
end

end

function printNumberOfDiscEv(algValues,algorithmNames)
numberAlg = length(algorithmNames);
[discEv,load] = getProcessedLastTimeStepCounts(algValues,numberAlg);

for i=1:length(algorithmNames)
   disp([algorithmNames{i},'   DiscEV: ',num2str(discEv(i)),'   Overload: ',num2str(load(i))]); 
end

end

function [discEv,load] = getProcessedLastTimeStepCounts(algValues,numberAlgs)
%The return is a cell: (numberAlgs,3,numTrials,numberValues,{value,meanReward})

numTrials = length(algValues{1});
discEv = zeros(1,numberAlgs);
load   = zeros(1,numberAlgs);

%Return one value for all three rewards for each X value on the graph
allTrialsDisc = zeros(numTrials,numberAlgs);
allTrialsLoad = zeros(numTrials,numberAlgs);

for i=1:numberAlgs
    for trial = 1:numTrials
        nValues = size(algValues{i}{trial},1);
        lines = cell2mat(algValues{i}{trial}{nValues,2});
        allTrialsDisc(trial,i) = sum(lines(:,3)); %Third metric is dischargedEVs
        allTrialsLoad(trial,i) = sum(lines(:,4)); %Fourth metric is overload
    end
end

discEv(:) = mean(allTrialsDisc,1);
load(:) = mean(allTrialsLoad,1);


end

function printRewardGraphs(algValues,algorithmNames)
%printRewardGraphs  Print the reward comparing algorithms by their
%accumulated reward


params = {'-or','-+g','-*b','-^k','--^k','--*b','--or','--+g'};
transparency = 0.3;


numberAlg = length(algorithmNames);
handlePlots = zeros(1,numberAlg);

[metrics,xValues] = getProcessedRewardMetrics(algValues,numberAlg);
for metric = 1:3
    %Create new plot canvas
    figure;
    hold all;
    for i=1:numberAlg
        %processedMetric = normalizeMetric(metrics,i,numberMetric);
        [minProcessedMetric,maxProcessedMetric,meanMetric] = normalizeMetric(metrics,i,metric);
       
       [~,handlePlots(i)] = shadedplot(xValues,maxProcessedMetric,minProcessedMetric,meanMetric,params{i},'k',transparency);
       set(handlePlots(i),'LineWidth',3);
       %plot(episodes{i},meanMetric);
    end
    legend_obj = legend(handlePlots, algorithmNames);
    graphConfiguration(metric,legend_obj);


end


end

function graphConfiguration(numberMetric,legendObj)
%"Set esthetic configurations

set(gca,'fontsize',22);
set(get(gca, 'children'),'markers',10);
grid on

xlabel('Training Days','FontName','Arial','FontWeight','bold');

%Q Table Entries
if(numberMetric==1)
    labelName='reward 1';
%Number of steps
elseif (numberMetric==2)
    labelName='reward 2';
%Cummulative Reward
elseif(numberMetric==3)
    labelName = 'reward 3';
end

set(legendObj,'FontSize',20);


ylabel(labelName,'FontWeight','bold','FontName','Arial','FontWeight','bold');
end

function [minProcessedMetric,maxProcessedMetric,meanMetric] = normalizeMetric(metrics,algNumber,numberMetric)
%Return mean of all trials

%(numberAlgs,3,numTrials,numberValues,{value,meanReward})

%One value for each xValue of each trial
allTrialsOneMetric  = zeros(size(metrics,3),size(metrics,4));

for trialIndex = 1:size(metrics,3)
    for xValueIndex = 1:size(metrics,4)
        allTrialsOneMetric(trialIndex,xValueIndex) = metrics{algNumber,numberMetric,trialIndex,xValueIndex,2};
    end
end
%Calculate mean
%processedMetric = nanmean(allTrialsOneMetric,1);
[minProcessedMetric,maxProcessedMetric,meanMetric] = confidence95intervals(allTrialsOneMetric,1);
end

function [metrics,xValues] = getProcessedRewardMetrics(algValues,numberAlgs)
%The return is a cell: (numberAlgs,3,numTrials,numberValues,{value,meanReward})

numTrials = length(algValues{1});

%Return one value for all three rewards for each X value on the graph
metrics = cell(numberAlgs,3,numTrials,0,2);

for i=1:numberAlgs
    for trial = 1:numTrials
        nValues = size(algValues{i}{trial},1);
        for valueIndex = 1:nValues
           value =  algValues{i}{trial}{valueIndex,1};
           lines = cell2mat(algValues{i}{trial}{valueIndex,2});
           reward3 = sum(lines(:,end));
           reward2 = sum(lines(:,end-1));
           reward1 = sum(lines(:,end-2));
           
           metrics{i,1,trial,valueIndex,1} = value;
           metrics{i,2,trial,valueIndex,1} = value;
           metrics{i,3,trial,valueIndex,1} = value;
           
           metrics{i,1,trial,valueIndex,2} = reward1;
           metrics{i,2,trial,valueIndex,2} = reward2;
           metrics{i,3,trial,valueIndex,2} = reward3;
        end
    end
end

xValues = zeros(1,size(metrics,4));
for i=1:length(xValues)
    xValues(i) = metrics{1,1,1,i,1};
end


end
function [trialsElement,metricNames] = getAlgMetrics(algName,trial)
% getAlgMetrics reads the values in an trial file
%   algName: Algorithm Name
%   trial to read

   
    %The return is a cell of two positions, the first is the number of
    %training steps and the second is all values for that given training
    %step
    trialsElement = cell(0,2);

    
    fileName = [num2str(trial),'.csv'];
    readF = importdata(fileName);
    metricNames = readF.colheaders(2:end);
    
    %Number of training days
    currentValue = readF.data(1,1);
    indexTrial = 1;
    currentTrialIndex = 1;
    currentTrial = cell(0,1);
    for index = 1:size(readF.data,1)
        if(currentValue ~= readF.data(index,1))
            trialsElement{indexTrial,1} = currentValue;
            trialsElement{indexTrial,2} = currentTrial;
            currentTrial = cell(0,1);
            currentValue = readF.data(index,1);
            indexTrial = indexTrial+1;
            currentTrialIndex = 1;
        end
        currentTrial{currentTrialIndex,1} = readF.data(index,2:end);
        currentTrialIndex = currentTrialIndex+1;
    end
    trialsElement{indexTrial,1} = currentValue;
    trialsElement{indexTrial,2} = currentTrial;
    
    

end