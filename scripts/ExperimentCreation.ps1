Param ($amount = 1, $batchCounterInit = 0, $experimentname = 0, $jarPath = 'CodeGenerator.jar', $throttlelimit = 2)

# Name of your directory on your execution server (for the table generation scripts)
$serverDirectory = '/home/xaver.fink'


# Experiement generation functions
Function fillerCode($i)
{
    $testname = 'FillerCode'
    $testsetNames = @('normal', 'Filler_PS_CN', 'Filler_PS_CI', 'Filler_PS_CO', 'Filler_PE_CN', 'Filler_PE_CI', 'Filler_PE_CO', 'Filler_PR_CN', 'Filler_PR_CI', 'Filler_PR_CO')
    $testsetName1 = 'normal'
    $testsetName2 = 'Filler_PS_CN'
    $testsetName3 = 'Filler_PS_CI'
    $testsetName4 = 'Filler_PS_CO'
    $testsetName5 = 'Filler_PE_CN'
    $testsetName6 = 'Filler_PE_CI'
    $testsetName7 = 'Filler_PE_CO'
    $testsetName8 = 'Filler_PR_CN'
    $testsetName9 = 'Filler_PR_CI'
    $testsetName10 = 'Filler_PR_CO'

    Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
        param($i, $testname, $testsetName1, $testsetName2, $testsetName3, $testsetName4, $testsetName5, $testsetName6, $testsetName7, $testsetName8, $testsetName9, $testsetName10, $jarPath, $throttlelimit)
        & java -jar $jarPath --createRandomRequirementCode --createTemplates --outputTemplate templates/Batch$i$testname --randomCodeAmount 50 --fileSuffix Batch$i --seed $i --generateAsManyAsNeeded --no-useProbabilityMap --connectRequirements --concatenateAllProperties;

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName1, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName1 -s OutputFiles/$testname --fileSuffix $testsetName1 --seed $i --no-stepLocalVariables --concatenateAllProperties;
        } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName1, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --fillerCode --fillerCodeAmount 50 --fillerCodePosition start --fillerCodeConnection none --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName2 -s OutputFiles/$testname --fileSuffix $testsetName2 --seed $i --no-stepLocalVariables --concatenateAllProperties;
        } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName3, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --fillerCode --fillerCodeAmount 50 --fillerCodePosition start --fillerCodeConnection input --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName3 -s OutputFiles/$testname --fileSuffix $testsetName3 --seed $i --no-stepLocalVariables --concatenateAllProperties;
        } -Arg $i, $testname, $testsetName3, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName4, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --fillerCode --fillerCodeAmount 50 --fillerCodePosition start --fillerCodeConnection output --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName4 -s OutputFiles/$testname --fileSuffix $testsetName4 --seed $i --no-stepLocalVariables --concatenateAllProperties;
        } -Arg $i, $testname, $testsetName4, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName5, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --fillerCode --fillerCodeAmount 50 --fillerCodePosition end --fillerCodeConnection none --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName5 -s OutputFiles/$testname --fileSuffix $testsetName5 --seed $i --no-stepLocalVariables --concatenateAllProperties;
        } -Arg $i, $testname, $testsetName5, $testsetName2, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName6, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --fillerCode --fillerCodeAmount 50 --fillerCodePosition end --fillerCodeConnection input --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName6 -s OutputFiles/$testname --fileSuffix $testsetName6 --seed $i --no-stepLocalVariables --concatenateAllProperties;
        } -Arg $i, $testname, $testsetName6, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName7, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --fillerCode --fillerCodeAmount 50 --fillerCodePosition end --fillerCodeConnection output --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName7 -s OutputFiles/$testname --fileSuffix $testsetName7 --seed $i --no-stepLocalVariables --concatenateAllProperties;
        } -Arg $i, $testname, $testsetName7, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName1, $testsetName8, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --fillerCode --fillerCodeAmount 50 --fillerCodePosition everywhere --fillerCodeConnection none --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName8 -s OutputFiles/$testname --fileSuffix $testsetName8 --seed $i --no-stepLocalVariables --concatenateAllProperties
        } -Arg $i, $testname, $testsetName1, $testsetName8, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName9, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --fillerCode --fillerCodeAmount 50 --fillerCodePosition everywhere --fillerCodeConnection input --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName9 -s OutputFiles/$testname --fileSuffix $testsetName9 --seed $i --no-stepLocalVariables --concatenateAllProperties;
        } -Arg $i, $testname, $testsetName9, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName10, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --fillerCode --fillerCodeAmount 50 --fillerCodePosition everywhere --fillerCodeConnection output --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName10 -s OutputFiles/$testname --fileSuffix $testsetName10 --seed $i --no-stepLocalVariables --concatenateAllProperties;
        } -Arg $i, $testname, $testsetName10, $jarPath


    } -Arg $i, $testname, $testsetName1, $testsetName2, $testsetName3, $testsetName4, $testsetName5, $testsetName6, $testsetName7, $testsetName8, $testsetName9, $testsetName10, $jarPath, $throttlelimit
}


Function codeModifications($i)
{
    $testname = 'CodeModifications'
    $testsetNames = @('normal', 'dependencies', 'functionizing', 'stepLocals')
    $testsetName1 = 'normal'
    $testsetName2 = 'dependencies'
    $testsetName3 = 'functionizing'
    $testsetName4 = 'stepLocals'

    & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
        param($i, $testname, $testsetName1, $jarPath, $opAmount, $throttlelimit)
        &    java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 50 -o OutputFiles/$testname/$testsetName1 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName1 --seed $i --no-useProbabilityMap --generateAsManyAsNeeded --concatenateAllProperties --no-stepLocalVariables;
    } -Arg $i, $testname, $testsetName1, $jarPath, $opAmount

    & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
        param($i, $testname, $testsetName2, $jarPath, $opAmount)
        &    java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 50 -o OutputFiles/$testname/$testsetName2 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName2 --seed $i --no-useProbabilityMap --generateAsManyAsNeeded --concatenateAllProperties --no-stepLocalVariables --connectRequirements;
    } -Arg $i, $testname, $testsetName2, $jarPath, $opAmount

    & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
        param($i, $testname, $testsetName3, $jarPath, $opAmount)
        &    java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 50 -o OutputFiles/$testname/$testsetName3 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName3 --seed $i --no-useProbabilityMap --generateAsManyAsNeeded --concatenateAllProperties --no-stepLocalVariables --functionizing;
    } -Arg $i, $testname, $testsetName3, $jarPath, $opAmount

    & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
        param($i, $testname, $testsetName4, $jarPath, $opAmount)
        &    java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 50 -o OutputFiles/$testname/$testsetName4 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName4 --seed $i --no-useProbabilityMap --generateAsManyAsNeeded --concatenateAllProperties;
    } -Arg $i, $testname, $testsetName4, $jarPath, $opAmount, $throttlelimit
}


Function floats($i)
{
    $testname = 'Floats'
    $testsetNames = @('has_floats', 'no_floats')
    $testsetName1 = 'has_floats'
    $testsetName2 = 'no_floats'

    Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
        param($i, $testname, $testsetName1, $testsetName2, $jarPath, $throttlelimit)
        & java -jar $jarPath --createRandomRequirementCode --createTemplates --outputTemplate templates/Batch$i$testname --randomCodeAmount 30 --fileSuffix Batch$i --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName1, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName1 -s OutputFiles/$testname --fileSuffix $testsetName1 --seed $i --concatenateAllProperties --no-stepLocalVariables;
        } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName1, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName2 -s OutputFiles/$testname --fileSuffix $testsetName2 --seed $i --concatenateAllProperties --no-stepLocalVariables --restrictToIntegerVariable;
        } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath


    } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath, $throttlelimit
}


Function kLoop($i)
{
    $testname = 'kloop'
    $testsetNames = @('25_1loop', '25_while', '50_1loop', '50_while', '100_1loop', '100_while')
    $testsetName1 = '25_1loop'
    $testsetName2 = '25_while'
    $testsetName3 = '50_1loop'
    $testsetName4 = '50_while'
    $testsetName5 = '100_1loop'
    $testsetName6 = '100_while'

    Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
        param($i, $testname, $testsetName1, $testsetName2, $jarPath, $throttlelimit)
        & java -jar $jarPath --createRandomRequirementCode --createTemplates --outputTemplate templates/Batch25$i$testname --randomCodeAmount 25 --fileSuffix Batch$i --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName1, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch25$i$testname -o OutputFiles/$testname/$testsetName1 -s OutputFiles/$testname --fileSuffix $testsetName1 --seed $i --concatenateAllProperties --no-stepLocalVariables --k-loop 1;
        } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName1, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch25$i$testname -o OutputFiles/$testname/$testsetName2 -s OutputFiles/$testname --fileSuffix $testsetName2 --seed $i --concatenateAllProperties --no-stepLocalVariables;
        } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath


    } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath, $throttlelimit


    Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
        param($i, $testname, $testsetName3, $testsetName4, $jarPath, $throttlelimit)
        & java -jar $jarPath --createRandomRequirementCode --createTemplates --outputTemplate templates/Batch50$i$testname --randomCodeAmount 50 --fileSuffix Batch$i --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName3, $testsetName4, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch50$i$testname -o OutputFiles/$testname/$testsetName3 -s OutputFiles/$testname --fileSuffix $testsetName3 --seed $i --concatenateAllProperties --no-stepLocalVariables --k-loop 1;
        } -Arg $i, $testname, $testsetName3, $testsetName4, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName3, $testsetName4, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch50$i$testname -o OutputFiles/$testname/$testsetName4 -s OutputFiles/$testname --fileSuffix $testsetName4 --seed $i --concatenateAllProperties --no-stepLocalVariables;
        } -Arg $i, $testname, $testsetName3, $testsetName4, $jarPath


    } -Arg $i, $testname, $testsetName3, $testsetName4, $jarPath, $throttlelimit


    Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
        param($i, $testname, $testsetName5, $testsetName6, $jarPath, $throttlelimit)
        & java -jar $jarPath --createRandomRequirementCode --createTemplates --outputTemplate templates/Batch100$i$testname --randomCodeAmount 100 --fileSuffix Batch$i --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName5, $testsetName6, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch100$i$testname -o OutputFiles/$testname/$testsetName5 -s OutputFiles/$testname --fileSuffix $testsetName5 --seed $i --concatenateAllProperties --no-stepLocalVariables --k-loop 1;
        } -Arg $i, $testname, $testsetName5, $testsetName6, $jarPath

        & Start-ThreadJob -ThrottleLimit $throttlelimit -ScriptBlock {
            param($i, $testname, $testsetName5, $testsetName6, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch100$i$testname -o OutputFiles/$testname/$testsetName6 -s OutputFiles/$testname --fileSuffix $testsetName6 --seed $i --concatenateAllProperties --no-stepLocalVariables;
        } -Arg $i, $testname, $testsetName5, $testsetName6, $jarPath


    } -Arg $i, $testname, $testsetName5, $testsetName6, $jarPath, $throttlelimit
}


Function operatorAmount($i)
{
    $testname = 'OperatorAmount'
    $testsetNames = @('Amount10', 'Amount25', 'Amount50', 'Amount100', 'Amount250', 'Amount500')
    $testsetName1 = 'Amount10'
    $testsetName2 = 'Amount25'
    $testsetName3 = 'Amount50'
    $testsetName4 = 'Amount100'
    $testsetName5 = 'Amount250'
    $testsetName6 = 'Amount500'

    Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
        param($i, $testname, $testsetName1, $jarPath)
        & java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 10 -o OutputFiles/$testname/$testsetName1 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName1 --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;
    } -Arg $i, $testname, $testsetName1, $jarPath

    Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
        param($i, $testname, $testsetName2, $jarPath)
        & java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 25 -o OutputFiles/$testname/$testsetName2 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName2 --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;
    } -Arg $i, $testname, $testsetName2, $jarPath

    Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
        param($i, $testname, $testsetName3, $jarPath)
        & java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 50 -o OutputFiles/$testname/$testsetName3 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName3 --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;
    } -Arg $i, $testname, $testsetName3, $jarPath

    Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
        param($i, $testname, $testsetName4, $jarPath)
        & java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 100 -o OutputFiles/$testname/$testsetName4 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName4 --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;
    } -Arg $i, $testname, $testsetName4, $jarPath

    Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
        param($i, $testname, $testsetName5, $jarPath)
        & java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 250 -o OutputFiles/$testname/$testsetName5 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName5 --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;
    } -Arg $i, $testname, $testsetName5, $jarPath

    Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
        param($i, $testname, $testsetName6, $jarPath)
        & java -jar $jarPath --createRandomRequirementCode --randomCodeAmount 500 -o OutputFiles/$testname/$testsetName6 -s OutputFiles/$testname --fileSuffix Batch$i$testsetName6 --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;
    } -Arg $i, $testname, $testsetName6, $jarPath
}


Function wrappers($i)
{
    $testname = 'Wrappers'
    $testsetNames = @('normal', 'Wrapper_S', 'Wrapper_A', 'Wrapper_P', 'Wrapper_SP', 'Wrapper_AP')
    $testsetName1 = 'normal'
    $testsetName2 = 'Wrapper_S'
    $testsetName3 = 'Wrapper_A'
    $testsetName4 = 'Wrapper_P'
    $testsetName5 = 'Wrapper_SP'
    $testsetName6 = 'Wrapper_AP'

    Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
        param($i, $testname, $testsetName1, $testsetName2, $testsetName3, $testsetName4, $testsetName5, $testsetName6, $jarPath)
        & java -jar $jarPath --createRandomRequirementCode --createTemplates --outputTemplate templates/Batch$i$testname --randomCodeAmount 30 --fileSuffix Batch$i --seed $i --generateAsManyAsNeeded --concatenateAllProperties --no-useProbabilityMap --connectRequirements;

        & Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
            param($i, $testname, $testsetName1, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName1 -s OutputFiles/$testname --fileSuffix $testsetName1 --seed $i --concatenateAllProperties --no-stepLocalVariables;
        } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath

        & Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
            param($i, $testname, $testsetName1, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName2 -s OutputFiles/$testname --fileSuffix $testsetName2 --seed $i --concatenateAllProperties --no-stepLocalVariables --wrapperStructs;
        } -Arg $i, $testname, $testsetName1, $testsetName2, $jarPath

        & Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
            param($i, $testname, $testsetName3, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName3 -s OutputFiles/$testname --fileSuffix $testsetName3 --seed $i --concatenateAllProperties --no-stepLocalVariables --wrapperArrays;
        } -Arg $i, $testname, $testsetName3, $jarPath

        & Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
            param($i, $testname, $testsetName4, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName4 -s OutputFiles/$testname --fileSuffix $testsetName4 --seed $i --concatenateAllProperties --no-stepLocalVariables --wrapperPointers;
        } -Arg $i, $testname, $testsetName4, $jarPath

        & Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
            param($i, $testname, $testsetName5, $testsetName2, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName5 -s OutputFiles/$testname --fileSuffix $testsetName5 --seed $i --concatenateAllProperties --no-stepLocalVariables --wrapperPointers --wrapperStructs;
        } -Arg $i, $testname, $testsetName5, $testsetName2, $jarPath

        & Start-ThreadJob -ThrottleLimit 15 -ScriptBlock {
            param($i, $testname, $testsetName6, $jarPath)
            &    java -jar $jarPath --createCodeFromTemplates --inputTemplates templates/Batch$i$testname -o OutputFiles/$testname/$testsetName6 -s OutputFiles/$testname --fileSuffix $testsetName6 --seed $i --concatenateAllProperties --no-stepLocalVariables --wrapperPointers --wrapperArrays;
        } -Arg $i, $testname, $testsetName6, $jarPath


    } -Arg $i, $testname, $testsetName1, $testsetName2, $testsetName3, $testsetName4, $testsetName5, $testsetName6, $jarPath
}
#########
######### NOTHING TO CONFIGURE BELOW
#########

# Names of verifiers
$verifiers = @("2LS", "CPA", "PESCO", "ESBMC", "SYMB", "UA")
$verifierArchives = @("2ls", "cpachecker", "pesco", "esbmc-kind", "symbiotic", "uautomizer")
$verifierNamesForXML = @("CPA", "PESCO", "2LS", "UA", "CBMC", "ESBMC", "SYMB", "COVERI")



# Initialize xml-writers (the writers will write configuration files for benchexec for 5 different model checkers)
$xmlWriters = New-Object System.Collections.ArrayList($null)

# Create some shell scripts (1. For preprocessing the verification tasks 2. For generating tables with the table generator after a verification run is completed)
Function createScripts($suffix)
{
    New-Item OutputFiles/$suffix -itemtype directory
    New-Item OutputFiles/$suffix/preprocess.sh
    Write-Output 'find . -type f -name "*.c" -exec sh -c ''gcc -E "$0" > "${0%.c}.i"'' {} '';''' |Out-File OutputFiles/$suffix/preprocess.sh -NoNewline

    New-Item OutputFiles/$suffix/tableGen.sh
    Write-Output ('find ' + $serverDirectory + '/Output -type f -name "*' + $suffix + '.*.xml.bz2" -exec sh -c ''table-generator "$0" '' {} '';'';
find ' + $serverDirectory + '/Output -type f -name "*' + $suffix + '.*.csv" -exec sh -c ''x="{}"; y=$(basename $x); z=$(dirname $x); mv "$x" "$z/${y#*.*.*.}"'' {} '';'';
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )";
python3 ' + $serverDirectory + '/GraphGenerator/ShellToGraphGeneratorBridge.py --path ' + $serverDirectory + '/Output --statsFolder $SCRIPT_DIR/stats --testName ' + $suffix) |Out-File OutputFiles/$suffix/tableGen.sh -NoNewline


    New-Item OutputFiles/$suffix/startVerifiers.sh
    $text = "#!/bin/bash`n"
    for ($i = 0; $i -lt $verifiers.length; $i++){
        $text += "mkdir " + $serverDirectory + "/bench-defs/custom-results/" + $suffix + "_" + $verifiers[$i] + "`n"
        $text += "tmux new-session -d -s " + $verifiers[$i] + "`n"
        $text += "tmux send-keys -t '" + $verifiers[$i] + "' 'cd " + $serverDirectory + "/bench-defs' Enter`n"
        $text += "tmux send-keys -t '" + $verifiers[$i] + "' 'scripts/execute-runs/execute-runcollection.sh benchexec/bin/benchexec archives/2022/" + $verifierArchives[$i] + ".zip custom-benchmarks/" + $suffix + "/setup" + $verifiers[$i] + ".xml witness.graphml .graphml custom-results/" + $suffix + "_" + $verifiers[$i] + "/ --read-only-dir / --read-only-dir /home --overlay-dir ./ --full-access-dir /sys/fs/cgroup' Enter`n"
    }
    Write-Output $text |Out-File OutputFiles/$suffix/startVerifiers.sh -NoNewline

}

# Functions for creation of the configuration files for benchexec and the 5 different model checkers

Function initXMLWriters($suffix)
{

    for ($i = 0; $i -lt 8; $i++){
        $writer = New-Object System.XMl.XmlTextWriter(($PSScriptRoot + "/OutputFiles/" + $suffix + "/setup" + $verifierNamesForXML[$i] + ".xml"), $Null)
        [void]($xmlWriters.Add($writer));

        $xmlWriters[$i].Formatting = 'Indented'

        $xmlWriters[$i].Indentation = 1

        $xmlWriters[$i].IndentChar = "`t"
    }

    xmlFilestart $suffix
}

Function xmlFilestart($suffix)
{
    $xmlWriters[0].WriteStartDocument()
    $doctypeParams = @("benchmark", "+//IDN sosy-lab.org//DTD BenchExec benchmark 2.3//EN", "https://www.sosy-lab.org/benchexec/benchmark-2.2.3dtd", $null)
    $xmlWriters[0].GetType().GetMethod("WriteDocType").Invoke($xmlWriters[0], $doctypeParams)
    $xmlWriters[0].WriteStartElement('benchmark')
    $xmlWriters[0].WriteAttributeString('tool', "cpachecker")
    $xmlWriters[0].WriteAttributeString('timelimit', "15 min")
    $xmlWriters[0].WriteAttributeString('hardtimelimit', "16 min")
    $xmlWriters[0].WriteAttributeString('memlimit', "15 GB")
    $xmlWriters[0].WriteAttributeString('cpuCores', "8")
    $xmlWriters[0].WriteElementString("resultfiles", "**.graphml")
    $xmlWriters[0].WriteElementString("propertyfile", '${taskdef_path}/reach.prp')
    $xmlWriters[0].WriteStartElement('option')
    $xmlWriters[0].WriteAttributeString('name', "-svcomp22")
    $xmlWriters[0].WriteEndElement()
    $xmlWriters[0].WriteStartElement('option')
    $xmlWriters[0].WriteAttributeString('name', "-heap")
    $xmlWriters[0].WriteRaw("10000M")
    $xmlWriters[0].WriteEndElement()
    $xmlWriters[0].WriteStartElement('option')
    $xmlWriters[0].WriteAttributeString('name', "-benchmark")
    $xmlWriters[0].WriteEndElement()
    $xmlWriters[0].WriteStartElement('option')
    $xmlWriters[0].WriteAttributeString('name', "-timelimit")
    $xmlWriters[0].WriteRaw("900 s")
    $xmlWriters[0].WriteEndElement()
    $xmlWriters[0].WriteStartElement('rundefinition')
    $xmlWriters[0].WriteAttributeString('name', "$suffix")


    $xmlWriters[1].WriteStartDocument()
    $doctypeParams = @("benchmark", "+//IDN sosy-lab.org//DTD BenchExec benchmark 2.3//EN", "https://www.sosy-lab.org/benchexec/benchmark-2.2.3dtd", $null)
    $xmlWriters[1].GetType().GetMethod("WriteDocType").Invoke($xmlWriters[1], $doctypeParams)
    $xmlWriters[1].WriteStartElement('benchmark')
    $xmlWriters[1].WriteAttributeString('tool', "pesco")
    $xmlWriters[1].WriteAttributeString('timelimit', "15 min")
    $xmlWriters[1].WriteAttributeString('hardtimelimit', "16 min")
    $xmlWriters[1].WriteAttributeString('memlimit', "15 GB")
    $xmlWriters[1].WriteAttributeString('cpuCores', "8")
    $xmlWriters[1].WriteElementString("resultfiles", "**.graphml")
    $xmlWriters[1].WriteElementString("propertyfile", '${taskdef_path}/reach.prp')
    $xmlWriters[1].WriteStartElement('option')
    $xmlWriters[1].WriteAttributeString('name', "-svcomp21-pesco")
    $xmlWriters[1].WriteEndElement()
    $xmlWriters[1].WriteStartElement('option')
    $xmlWriters[1].WriteAttributeString('name', "-heap")
    $xmlWriters[1].WriteRaw("10000M")
    $xmlWriters[1].WriteEndElement()
    $xmlWriters[1].WriteStartElement('option')
    $xmlWriters[1].WriteAttributeString('name', "-stack")
    $xmlWriters[1].WriteRaw("2048k")
    $xmlWriters[1].WriteEndElement()
    $xmlWriters[1].WriteStartElement('option')
    $xmlWriters[1].WriteAttributeString('name', "-benchmark")
    $xmlWriters[1].WriteEndElement()
    $xmlWriters[1].WriteStartElement('option')
    $xmlWriters[1].WriteAttributeString('name', "-timelimit")
    $xmlWriters[1].WriteRaw("900 s")
    $xmlWriters[1].WriteEndElement()
    $xmlWriters[1].WriteStartElement('rundefinition')
    $xmlWriters[1].WriteAttributeString('name', "$suffix")


    $xmlWriters[2].WriteStartDocument()
    $doctypeParams = @("benchmark", "+//IDN sosy-lab.org//DTD BenchExec benchmark 2.3//EN", "https://www.sosy-lab.org/benchexec/benchmark-2.2.3dtd", $null)
    $xmlWriters[2].GetType().GetMethod("WriteDocType").Invoke($xmlWriters[2], $doctypeParams)
    $xmlWriters[2].WriteStartElement('benchmark')
    $xmlWriters[2].WriteAttributeString('tool', "two_ls")
    $xmlWriters[2].WriteAttributeString('timelimit', "15 min")
    $xmlWriters[2].WriteAttributeString('memlimit', "15 GB")
    $xmlWriters[2].WriteAttributeString('cpuCores', "8")
    $xmlWriters[2].WriteElementString("resultfiles", "**.graphml")
    $xmlWriters[2].WriteElementString("propertyfile", '${taskdef_path}/reach.prp')
    $xmlWriters[2].WriteStartElement('option')
    $xmlWriters[2].WriteAttributeString('name', "--graphml-witness")
    $xmlWriters[2].WriteRaw("witness.graphml")
    $xmlWriters[2].WriteEndElement()
    $xmlWriters[2].WriteStartElement('rundefinition')
    $xmlWriters[2].WriteAttributeString('name', "$suffix")


    $xmlWriters[3].WriteStartDocument()
    $doctypeParams = @("benchmark", "+//IDN sosy-lab.org//DTD BenchExec benchmark 2.3//EN", "https://www.sosy-lab.org/benchexec/benchmark-2.2.3dtd", $null)
    $xmlWriters[3].GetType().GetMethod("WriteDocType").Invoke($xmlWriters[3], $doctypeParams)
    $xmlWriters[3].WriteStartElement('benchmark')
    $xmlWriters[3].WriteAttributeString('tool', "ultimateautomizer")
    $xmlWriters[3].WriteAttributeString('timelimit', "15 min")
    $xmlWriters[3].WriteAttributeString('hardtimelimit', "16 min")
    $xmlWriters[3].WriteAttributeString('memlimit', "15 GB")
    $xmlWriters[3].WriteAttributeString('cpuCores', "8")
    $xmlWriters[3].WriteElementString("resultfiles", "**.graphml")
    $xmlWriters[3].WriteElementString("propertyfile", '${taskdef_path}/reach.prp')
    $xmlWriters[3].WriteStartElement('option')
    $xmlWriters[3].WriteAttributeString('name', "--full-output")
    $xmlWriters[3].WriteEndElement()
    $xmlWriters[3].WriteStartElement('rundefinition')
    $xmlWriters[3].WriteAttributeString('name', "$suffix")


    $xmlWriters[4].WriteStartDocument()
    $doctypeParams = @("benchmark", "+//IDN sosy-lab.org//DTD BenchExec benchmark 2.3//EN", "https://www.sosy-lab.org/benchexec/benchmark-2.2.3dtd", $null)
    $xmlWriters[4].GetType().GetMethod("WriteDocType").Invoke($xmlWriters[4], $doctypeParams)
    $xmlWriters[4].WriteStartElement('benchmark')
    $xmlWriters[4].WriteAttributeString('tool', "cbmc")
    $xmlWriters[4].WriteAttributeString('timelimit', "15 min")
    $xmlWriters[4].WriteAttributeString('hardtimelimit', "16 min")
    $xmlWriters[4].WriteAttributeString('memlimit', "15 GB")
    $xmlWriters[4].WriteAttributeString('cpuCores', "8")
    $xmlWriters[4].WriteElementString("resultfiles", "**.graphml")
    $xmlWriters[4].WriteElementString("propertyfile", '${taskdef_path}/reach.prp')
    $xmlWriters[4].WriteStartElement('option')
    $xmlWriters[4].WriteAttributeString('name', "--graphml-witness")
    $xmlWriters[4].WriteRaw("witness.graphml")
    $xmlWriters[4].WriteEndElement()
    $xmlWriters[4].WriteStartElement('option')
    $xmlWriters[4].WriteAttributeString('name', "--32")
    $xmlWriters[4].WriteEndElement()
    $xmlWriters[4].WriteStartElement('rundefinition')
    $xmlWriters[4].WriteAttributeString('name', "$suffix")


    $xmlWriters[5].WriteStartDocument()
    $doctypeParams = @("benchmark", "+//IDN sosy-lab.org//DTD BenchExec benchmark 2.3//EN", "https://www.sosy-lab.org/benchexec/benchmark-2.2.3dtd", $null)
    $xmlWriters[5].GetType().GetMethod("WriteDocType").Invoke($xmlWriters[5], $doctypeParams)
    $xmlWriters[5].WriteStartElement('benchmark')
    $xmlWriters[5].WriteAttributeString('tool', "esbmc")
    $xmlWriters[5].WriteAttributeString('timelimit', "15 min")
    $xmlWriters[5].WriteAttributeString('memlimit', "15 GB")
    $xmlWriters[5].WriteAttributeString('cpuCores', "8")
    $xmlWriters[5].WriteElementString("resultfiles", "**.graphml")
    $xmlWriters[5].WriteElementString("propertyfile", '${taskdef_path}/reach.prp')
    $xmlWriters[5].WriteStartElement('option')
    $xmlWriters[5].WriteAttributeString('name', "-s")
    $xmlWriters[5].WriteRaw("kinduction")
    $xmlWriters[5].WriteEndElement()
    $xmlWriters[5].WriteStartElement('rundefinition')
    $xmlWriters[5].WriteAttributeString('name', "$suffix")


    $xmlWriters[6].WriteStartDocument()
    $doctypeParams = @("benchmark", "+//IDN sosy-lab.org//DTD BenchExec benchmark 2.3//EN", "https://www.sosy-lab.org/benchexec/benchmark-2.2.3dtd", $null)
    $xmlWriters[6].GetType().GetMethod("WriteDocType").Invoke($xmlWriters[6], $doctypeParams)
    $xmlWriters[6].WriteStartElement('benchmark')
    $xmlWriters[6].WriteAttributeString('tool', "symbiotic")
    $xmlWriters[6].WriteAttributeString('timelimit', "15 min")
    $xmlWriters[6].WriteAttributeString('memlimit', "15 GB")
    $xmlWriters[6].WriteAttributeString('cpuCores', "8")
    $xmlWriters[6].WriteElementString("resultfiles", "**.graphml")
    $xmlWriters[6].WriteElementString("propertyfile", '${taskdef_path}/reach.prp')
    $xmlWriters[6].WriteStartElement('option')
    $xmlWriters[6].WriteAttributeString('name', "--witness")
    $xmlWriters[6].WriteRaw("witness.graphml")
    $xmlWriters[6].WriteEndElement()
    $xmlWriters[6].WriteStartElement('option')
    $xmlWriters[6].WriteAttributeString('name', "--sv-comp")
    $xmlWriters[6].WriteEndElement()
    $xmlWriters[6].WriteStartElement('rundefinition')
    $xmlWriters[6].WriteAttributeString('name', "$suffix")


    $xmlWriters[7].WriteStartDocument()
    $doctypeParams = @("benchmark", "+//IDN sosy-lab.org//DTD BenchExec benchmark 2.3//EN", "https://www.sosy-lab.org/benchexec/benchmark-2.2.3dtd", $null)
    $xmlWriters[7].GetType().GetMethod("WriteDocType").Invoke($xmlWriters[7], $doctypeParams)
    $xmlWriters[7].WriteStartElement('benchmark')
    $xmlWriters[7].WriteAttributeString('tool', "coveriteam-verifier-validator")
    $xmlWriters[7].WriteAttributeString('displayName', "CoVeriTeam-Verifier-ParPortfolio")
    $xmlWriters[7].WriteAttributeString('timelimit', "15 min")
    $xmlWriters[7].WriteAttributeString('hardtimelimit', "16 min")
    $xmlWriters[7].WriteAttributeString('memlimit', "15 GB")
    $xmlWriters[7].WriteAttributeString('cpuCores', "8")
    $xmlWriters[7].WriteElementString("resultfiles", "**/*.graphml")
    $xmlWriters[7].WriteElementString("propertyfile", '${taskdef_path}/reach.prp')
    $xmlWriters[7].WriteStartElement('option')
    $xmlWriters[7].WriteAttributeString('name', "verifier-parallel-portfolio.cvt")
    $xmlWriters[7].WriteEndElement()
    $xmlWriters[7].WriteStartElement('option')
    $xmlWriters[7].WriteAttributeString('name', "--cache-dir")
    $xmlWriters[7].WriteRaw("cache")
    $xmlWriters[7].WriteEndElement()
    $xmlWriters[7].WriteStartElement('option')
    $xmlWriters[7].WriteAttributeString('name', "--no-cache-update")
    $xmlWriters[7].WriteEndElement()
    $xmlWriters[7].WriteStartElement('option')
    $xmlWriters[7].WriteAttributeString('name', "--use-python-processes")
    $xmlWriters[7].WriteEndElement()
    $xmlWriters[7].WriteStartElement('rundefinition')
    $xmlWriters[7].WriteAttributeString('name', "$suffix")

}

Function createXMLTask($name)
{
    for ($i = 0; $i -lt 8; $i++){
        $xmlWriters[$i].WriteStartElement('tasks')
        $xmlWriters[$i].WriteAttributeString('name', "$name")
        $xmlWriters[$i].WriteElementString("include", "$name/*.yml")
        $xmlWriters[$i].WriteEndElement()
    }
}

Function xmlFileend()
{
    for ($i = 0; $i -lt 8; $i++){
        $xmlWriters[$i].WriteEndElement()
        $xmlWriters[$i].WriteEndElement()
        $xmlWriters[$i].WriteEndDocument()
        $xmlWriters[$i].Flush()
        $xmlWriters[$i].Close()
    }
}


switch ($experimentname)
{
    0{
        break
    }

    fillercode{
        $testname = 'FillerCode'
        $testsetNames = @('normal', 'Filler_PS_CN', 'Filler_PS_CI', 'Filler_PS_CO', 'Filler_PE_CN', 'Filler_PE_CI', 'Filler_PE_CO', 'Filler_PR_CN', 'Filler_PR_CI', 'Filler_PR_CO')
        $testsetName1 = 'normal'
        $testsetName2 = 'Filler_PS_CN'
        $testsetName3 = 'Filler_PS_CI'
        $testsetName4 = 'Filler_PS_CO'
        $testsetName5 = 'Filler_PE_CN'
        $testsetName6 = 'Filler_PE_CI'
        $testsetName7 = 'Filler_PE_CO'
        $testsetName8 = 'Filler_PR_CN'
        $testsetName9 = 'Filler_PR_CI'
        $testsetName10 = 'Filler_PR_CO'
        for($i = $batchCounterInit; $i -lt ($amount + $batchCounterInit); $i++){
            fillerCode $i;
        }
        createScripts $testname
        initXMLWriters $testname
        foreach ($j in $testsetNames)
        {
            createXMLTask $j
        }
    }

    codemodifications{
        $testname = 'CodeModifications'
        $testsetNames = @('normal', 'dependencies', 'functionizing', 'stepLocals')
        $testsetName1 = 'normal'
        $testsetName2 = 'dependencies'
        $testsetName3 = 'functionizing'
        $testsetName4 = 'stepLocals'
        for($i = $batchCounterInit; $i -lt ($amount + $batchCounterInit); $i++){
            codeModifications $i;
        }
        createScripts $testname
        initXMLWriters $testname
        foreach ($j in $testsetNames)
        {
            createXMLTask $j
        }
    }

    floats{
        $testname = 'Floats'
        $testsetNames = @('has_floats', 'no_floats')
        $testsetName1 = 'has_floats'
        $testsetName2 = 'no_floats'
        for($i = $batchCounterInit; $i -lt ($amount + $batchCounterInit); $i++){
            floats $i;
        }
        createScripts $testname
        initXMLWriters $testname
        foreach ($j in $testsetNames)
        {
            createXMLTask $j
        }
    }

    kloop{
        $testname = 'kloop'
        $testsetNames = @('25_1loop', '25_while', '50_1loop', '50_while', '100_1loop', '100_while')
        $testsetName1 = '25_1loop'
        $testsetName2 = '25_while'
        $testsetName3 = '50_1loop'
        $testsetName4 = '50_while'
        $testsetName5 = '100_1loop'
        $testsetName6 = '100_while'
        for($i = $batchCounterInit; $i -lt ($amount + $batchCounterInit); $i++){
            kLoop $i;
        }
        createScripts $testname
        initXMLWriters $testname
        foreach ($j in $testsetNames)
        {
            createXMLTask $j
        }
    }

    operatoramount{
        $testname = 'OperatorAmount'
        $testsetNames = @('Amount10', 'Amount25', 'Amount50', 'Amount100', 'Amount250', 'Amount500')
        $testsetName1 = 'Amount10'
        $testsetName2 = 'Amount25'
        $testsetName3 = 'Amount50'
        $testsetName4 = 'Amount100'
        $testsetName5 = 'Amount250'
        $testsetName6 = 'Amount500'
        for($i = $batchCounterInit; $i -lt ($amount + $batchCounterInit); $i++){
            operatorAmount $i;
        }
        createScripts $testname
        initXMLWriters $testname
        foreach ($j in $testsetNames)
        {
            createXMLTask $j
        }
    }

    wrappers{
        $testname = 'Wrappers'
        $testsetNames = @('normal', 'Wrapper_S', 'Wrapper_A', 'Wrapper_P', 'Wrapper_SP', 'Wrapper_AP')
        $testsetName1 = 'normal'
        $testsetName2 = 'Wrapper_S'
        $testsetName3 = 'Wrapper_A'
        $testsetName4 = 'Wrapper_P'
        $testsetName5 = 'Wrapper_SP'
        $testsetName6 = 'Wrapper_AP'
        for($i = $batchCounterInit; $i -lt ($amount + $batchCounterInit); $i++){
            wrappers $i;
        }
        createScripts $testname
        initXMLWriters $testname
        foreach ($j in $testsetNames)
        {
            createXMLTask $j
        }
    }

}

xmlFileend






