# Basic usage

To generate one verification task, execute the following command:
```
CodeGenerator.jar --createRandomRequirementCode
```
Generally you want to specify the size of the generated code, a seed for the random-generation, and a String suffix for the generated files.
```
CodeGenerator.jar --createRandomRequirementCode --randomCodeAmount <amount:int> --seed <seed:int> --fileSuffix <name:Chars>
```

# Experiment Execution and Reproduction

To reproduce the verification task sets the `ExperimentCreation.ps1` script in the scripts folder should be used. Powershell 7.2 is required. 
The script takes the following relevant parameters:
- `experimentname <[floats, fillercode, codemodifications, kloop, operatoramount, wrappers]>`: The experiment configuration
- `-amount <int>`: Number of verification tasks to generate. For a reproduction use `100`.

Optionally you can set the following parameters:
- `-jarPath`: Sets the path of the used `CodeGenerator.jar`.
- `-throttlelimit <int, default=4>`: Limits the number of threads for the generation running similtaniously. Set this lower if you are experiencing problems like freezing or higher for a faster generation if you are confident in your hardware.
- `-batchCounterInit <int>`: Sets the starting seed for the generation of verification tasks. For a reproduction leave this at zero. Set it to a higher number if you want to generate new verification tasks.

## Example
For a reproduction of the Float-Experiment the following command would be used:
```
ExperimentCreation.ps1 -amount 100 -experimentname floats
```
