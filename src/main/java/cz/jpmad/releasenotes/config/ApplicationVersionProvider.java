package cz.jpmad.releasenotes.config;

import picocli.CommandLine;

public class ApplicationVersionProvider
        implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() {
        return new String[]{
                "Release Notes Generator "
                        + ApplicationVersion.get()
        };
    }
}