package net.ihe.gazelle.tm.test;

import org.junit.Ignore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Ignore
public class ProcessGazelle implements Runnable {

    private static Pattern PATTERN = Pattern.compile("TM\\/testInstance\\.seam\\?id=(\\d*)");
    private static Random RANDOM = new Random();

    private GazelleClient gazelleClient;
    private ExecutorService threadPool;
    private List<String> commands;

    public ProcessGazelle(ExecutorService threadPool, GazelleClient gazelleClient, List<String> commands) {
        super();
        this.threadPool = threadPool;
        this.gazelleClient = gazelleClient;
        this.commands = new ArrayList<String>(commands);
    }

    @Override
    public void run() {
        List<String> newCommands = new ArrayList<String>(commands);
        if (commands.size() > 0) {
            String command = commands.get(0);
            try {
                if ("start".equals(command)) {
                    gazelleClient.startUp();
                }
                if (command.startsWith("ti-")) {
                    String tiNumber = command.substring(3);
                    gazelleClient.ti(tiNumber);
                }
                if ("configs".equals(command)) {
                    gazelleClient.configs();
                }
                if ("users".equals(command)) {
                    gazelleClient.users();
                }
                if ("companies".equals(command)) {
                    gazelleClient.companies();
                }
                if ("cat".equals(command)) {
                    String cat = gazelleClient.cat();

                    Matcher matcher = PATTERN.matcher(cat);

                    List<String> matches = new ArrayList<String>();
                    while (matcher.find()) {
                        matches.add(matcher.group(1));
                    }

                    if (matches.size() > 0) {
                        String tiNumber = matches.get(RANDOM.nextInt(matches.size()));
                        newCommands.add("ti-" + tiNumber);
                    }
                }
                randomNext(newCommands);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        newCommands.remove(0);
        // Thread.sleep(200 + RANDOM.nextInt(500));
        synchronized (threadPool) {
            threadPool.execute(new ProcessGazelle(threadPool, gazelleClient, newCommands));
        }
    }

    public void randomNext(List<String> newCommands) {
        int nextInt = RANDOM.nextInt(4);
        switch (nextInt) {
            case 0:
                newCommands.add("cat");
                break;
            case 1:
                newCommands.add("configs");
                break;
            case 2:
                newCommands.add("users");
                break;
            case 3:
                newCommands.add("companies");
                break;
            default:
                break;
        }
    }
}
