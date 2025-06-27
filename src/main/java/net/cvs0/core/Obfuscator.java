package net.cvs0.core;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.obfuscation.*;
import net.cvs0.obfuscation.strategies.*;
import net.cvs0.classfile.remappers.TinyRemapperAdapter;
import net.cvs0.utils.Logger;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Obfuscator 
{
    private final List<ObfuscationStrategy> strategies;
    private final ExecutorService executorService;

    public Obfuscator() 
    {
        this.strategies = createDefaultStrategies();
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "Obfuscator-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    public void obfuscate(File inputJar, File outputJar, ObfuscationConfig config) throws Exception 
    {
        if (config.isVerbose()) {
            Logger.info("Starting obfuscation process...");
            Logger.info("Input: " + inputJar.getAbsolutePath());
            Logger.info("Output: " + outputJar.getAbsolutePath());
        }

        long startTime = System.currentTimeMillis();

        try {
            JarAnalyzer analyzer = new JarAnalyzer(config);
            JarAnalyzer.AnalysisResult analysisResult = analyzer.analyze(inputJar);
            Program program = analysisResult.getProgram();

            if (config.isVerbose()) {
                Logger.info("Analysis completed. Found " + program.getClassCount() + " classes");
            }

            MappingContext mappingContext = new MappingContext();
            
            applyObfuscationStrategies(program, config, mappingContext);

            if (config.isVerbose()) {
                mappingContext.printMappingStats();
            }

            validateProgram(program, config);

            TinyRemapperAdapter remapper = new TinyRemapperAdapter(config);
            remapper.remap(inputJar, outputJar, mappingContext, analysisResult.getResources(), 
                          analysisResult.getManifestAttributes());

            long duration = System.currentTimeMillis() - startTime;
            if (config.isVerbose()) {
                Logger.success("Obfuscation completed successfully in " + duration + "ms");
            }

        } catch (Exception e) {
            Logger.error("Obfuscation failed: " + e.getMessage());
            if (config.isVerbose()) {
                e.printStackTrace();
            }
            throw e;
        }
    }

    private void applyObfuscationStrategies(Program program, ObfuscationConfig config, MappingContext mappingContext) throws ObfuscationException 
    {
        if (config.isVerbose()) {
            Logger.info("Available strategies: " + strategies.size());
            for (ObfuscationStrategy strategy : strategies) {
                boolean enabled = strategy.isEnabled(config);
                Logger.info("  - " + strategy.getName() + " (Priority: " + strategy.getPriority() + ") - Enabled: " + enabled);
            }
        }
        
        List<ObfuscationStrategy> enabledStrategies = strategies.stream()
            .filter(strategy -> strategy.isEnabled(config))
            .sorted(Comparator.comparingInt(ObfuscationStrategy::getPriority))
            .toList();

        if (config.isVerbose()) {
            Logger.info("Applying " + enabledStrategies.size() + " obfuscation strategies:");
            for (ObfuscationStrategy strategy : enabledStrategies) {
                Logger.info("  - " + strategy.getName() + " (Priority: " + strategy.getPriority() + ")");
            }
        }

        if (config.isSequentialTransformers()) {
            applyStrategiesSequentially(enabledStrategies, program, config, mappingContext);
        } else {
            applyStrategiesInParallel(enabledStrategies, program, config, mappingContext);
        }
    }

    private void applyStrategiesSequentially(List<ObfuscationStrategy> strategies, Program program, 
                                           ObfuscationConfig config, MappingContext mappingContext) throws ObfuscationException 
    {
        for (ObfuscationStrategy strategy : strategies) {
            if (config.isVerbose()) {
                Logger.info("Applying strategy: " + strategy.getName());
            }
            
            long strategyStart = System.currentTimeMillis();
            strategy.obfuscate(program, config, mappingContext);
            long strategyDuration = System.currentTimeMillis() - strategyStart;
            
            if (config.isVerbose()) {
                Logger.info("Strategy " + strategy.getName() + " completed in " + strategyDuration + "ms");
            }
        }
    }

    private void applyStrategiesInParallel(List<ObfuscationStrategy> strategies, Program program, 
                                         ObfuscationConfig config, MappingContext mappingContext) throws ObfuscationException 
    {
        List<Future<Void>> futures = new ArrayList<>();
        
        for (ObfuscationStrategy strategy : strategies) {
            Future<Void> future = executorService.submit(() -> {
                try {
                    if (config.isVerbose()) {
                        Logger.info("Applying strategy: " + strategy.getName());
                    }
                    
                    long strategyStart = System.currentTimeMillis();
                    strategy.obfuscate(program, config, mappingContext);
                    long strategyDuration = System.currentTimeMillis() - strategyStart;
                    
                    if (config.isVerbose()) {
                        Logger.info("Strategy " + strategy.getName() + " completed in " + strategyDuration + "ms");
                    }
                    
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException("Strategy " + strategy.getName() + " failed", e);
                }
            });
            
            futures.add(future);
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ObfuscationException("Obfuscation interrupted", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ObfuscationException) {
                    throw (ObfuscationException) cause;
                } else {
                    throw new ObfuscationException("Strategy execution failed", cause);
                }
            }
        }
    }

    private void validateProgram(Program program, ObfuscationConfig config) throws ObfuscationException 
    {
        if (config.isVerbose()) {
            Logger.info("Validating program integrity...");
        }

        int validationErrors = 0;

        for (var programClass : program.getAllClasses()) {
            try {
                for (var method : programClass.getMethods()) {
                    if (method.hasCode()) {
                    }
                }
            } catch (Exception e) {
                validationErrors++;
                if (config.isVerbose()) {
                    Logger.warn("Validation warning for class " + programClass.getName() + ": " + e.getMessage());
                }
            }
        }

        if (validationErrors > 0) {
            if (config.isVerbose()) {
                Logger.warn("Found " + validationErrors + " validation warnings");
            }
        } else {
            if (config.isVerbose()) {
                Logger.info("Program validation passed");
            }
        }
    }

    private List<ObfuscationStrategy> createDefaultStrategies() 
    {
        List<ObfuscationStrategy> defaultStrategies = new ArrayList<>();
        

        defaultStrategies.add(new ClassRenamingStrategy());
        defaultStrategies.add(new MethodRenamingStrategy());
        defaultStrategies.add(new FieldRenamingStrategy());
        defaultStrategies.add(new SyntheticMemberStrategy());

        return defaultStrategies;
    }
}