package org.develrulez.thinjar.util;

import java.util.Locale;

public class OperatingSystem {

    public enum Type {
        WINDOWS,
        UNIX,
        POSIX_UNIX,
        MAC,
        OTHER;
    }

    private final Type type;
    private final String version;

    private static final LazyInitializer<OperatingSystem> INSTANCE = new LazyInitializer<OperatingSystem>() {
        @Override
        protected OperatingSystem initialize() {
            Type type;
            String version;
            try {
                String osName = System.getProperty("os.name");
                if (osName == null) {
                    throw new IllegalStateException("System property 'os.name' not found");
                }
                osName = osName.toLowerCase(Locale.ENGLISH);

                if (osName.contains("windows")) {
                    type = Type.WINDOWS;
                } else if (osName.contains("linux")
                        || osName.contains("mpe/ix")
                        || osName.contains("freebsd")
                        || osName.contains("irix")
                        || osName.contains("digital unix")
                        || osName.contains("unix")) {
                    type = Type.UNIX;
                } else if (osName.contains("mac os")) {
                    type = Type.MAC;
                } else if (osName.contains("sun os")
                        || osName.contains("sunos")
                        || osName.contains("solaris")) {
                    type = Type.POSIX_UNIX;
                } else if (osName.contains("hp-ux")
                        || osName.contains("aix")) {
                    type = Type.POSIX_UNIX;
                } else {
                    type = Type.OTHER;
                }

            } catch (Exception ex) {
                type = Type.OTHER;
            } finally {
                version = System.getProperty("os.version");
            }
            return new OperatingSystem(type, version);
        }
    };

    private OperatingSystem(Type type, String version) {
        this.type = type;
        this.version = version;
    }

    public static OperatingSystem get(){
        return INSTANCE.get();
    }

    public Type getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public boolean isUnix(){
        return Type.UNIX.equals(type);
    }

    public boolean isWindows(){
        return Type.WINDOWS.equals(type);
    }

    public boolean isMac(){
        return Type.MAC.equals(type);
    }
}
