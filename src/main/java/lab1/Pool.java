package lab1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class Pool {

    public static void main(String[] args) {
        try {
            System.out.println("Constant pool:");
            FileInputStream fileInputStream = new FileInputStream(new File(args[0]));

            Vector<ConstantNode> nodes = read(fileInputStream);

            int v = 1;
            for (ConstantNode node : nodes) {
                if (node.getTag() == null) {
                    v++;
                    continue;
                }
                System.out.print("#" + v++ + " = " + node.getTag() + "   ");
                for (ResolveData h : node.getRefs()) {
                    System.out.print(h.getData());
                }
                if (node.getRefs().get(0) instanceof StaticData && node.getRefs().size() == 1) {
                    System.out.println();
                } else System.out.println("    // " + ConstantNode.resolveNote(nodes, nodes.indexOf(node)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("File doesn't exist");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Access to class file not allowed or stream error");
        }
    }

    private static boolean readHandle(InputStream inputStream) throws Exception {
        byte[] buf = new byte[8];
        int res = inputStream.read(buf);
        if (res == 8) {
            return true;
        }
        throw new Exception();
    }

    private static Vector<ConstantNode> read(InputStream in) throws Exception {
        if (readHandle(in))
            return compute(in);
        throw new Exception();
    }

    private static Vector<ConstantNode> compute(InputStream inputStream) throws Exception {
        Vector<ConstantNode> pool = new Vector<>();

        int tag;
        int y = computeSize(inputStream);
        while (--y != 0) {
            tag = inputStream.read();
            Tags t = Tags.getById(tag);

            pool.add(t.compute(inputStream));

            if (t.equals(Tags.LONG) || t.equals(Tags.DOUBLE)) {
                pool.add(new ConstantNode(null, null));
                y--;
            }
        }
        return pool;
    }

    private static int computeSize(InputStream inputStream) throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        return ((0xFF & b1) << 8) | (0xFF & b2);
    }
}

class ConstantNode {
    private Tags tag;
    private Vector<ResolveData> refs;

    Tags getTag() {
        return tag;
    }

    Vector<ResolveData> getRefs() {
        return refs;
    }


    ConstantNode(Tags tag, Vector<ResolveData> refs) {
        this.tag = tag;
        this.refs = refs;
    }

    static String resolveNote(Vector<ConstantNode> nodes, int pos) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ResolveData s : nodes.get(pos).refs) {
            stringBuilder.append(s.resolve(nodes));
        }
        return stringBuilder.toString();
    }
}

class Ref extends ResolveData {
    Ref(String toResolve) {
        super(toResolve);
    }

    @Override
    public String getData() {
        return "#" + toResolve;
    }

    @Override
    public String resolve(Vector<ConstantNode> nodes) {
        StringBuilder stringBuilder = new StringBuilder();
        if ((Integer.parseInt(toResolve) - 1) >= nodes.size()) return stringBuilder.toString();
        ConstantNode c = nodes.get(Integer.parseInt(toResolve) - 1);
        for (ResolveData s : c.getRefs()) {
            stringBuilder.append(s.resolve(nodes));
        }
        return stringBuilder.toString();
    }
}

abstract class ResolveData {
    String toResolve;

    ResolveData(String toResolve) {
        this.toResolve = toResolve;
    }

    public abstract String getData();

    public abstract String resolve(Vector<ConstantNode> nodes);
}

class StaticData extends ResolveData {
    StaticData(String toResolve) {
        super(toResolve);
    }

    @Override
    public String getData() {
        return toResolve;
    }

    @Override
    public String resolve(Vector<ConstantNode> nodes) {
        return toResolve;
    }
}

enum Tags {
    UTF8(1) {
        public ConstantNode compute(InputStream inputStream) throws IOException {
            Vector<ResolveData> refs = new Vector<>();
            int count = twoByteInInt((byte) inputStream.read(), (byte) inputStream.read());
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < count; i++) {
                stringBuilder.appendCodePoint((byte) inputStream.read());
            }
            refs.add(new StaticData(stringBuilder.toString()));
            return new ConstantNode(Tags.UTF8, refs);
        }
    },
    INTEGER(3) {
        public ConstantNode compute(InputStream inputStream) throws IOException {
            Vector<ResolveData> refs = new Vector<>();
            refs.add(new StaticData(fourByteInInt((byte) inputStream.read(), (byte) inputStream.read(),
                    (byte) inputStream.read(), (byte) inputStream.read()).toString()));
            return new ConstantNode(Tags.INTEGER, refs);
        }
    },
    LONG(5) {
        public ConstantNode compute(InputStream inputStream) throws IOException {
            Vector<ResolveData> refs = new Vector<>();
            int highb = fourByteInInt((byte) inputStream.read(), (byte) inputStream.read(),
                    (byte) inputStream.read(), (byte) inputStream.read());
            int lowb = fourByteInInt((byte) inputStream.read(), (byte) inputStream.read(),
                    (byte) inputStream.read(), (byte) inputStream.read());
            long l = ((long) highb << 32) + lowb;

            refs.add(new StaticData(Long.toString(l)));

            return new ConstantNode(Tags.LONG, refs);
        }
    },
    DOUBLE(6) {
        public ConstantNode compute(InputStream inputStream) throws IOException {
            Vector<ResolveData> refs = new Vector<>();
            refs.add(new StaticData(fourByteInInt((byte) inputStream.read(), (byte) inputStream.read(),
                    (byte) inputStream.read(), (byte) inputStream.read()).toString()));
            refs.add(new StaticData(fourByteInInt((byte) inputStream.read(), (byte) inputStream.read(),
                    (byte) inputStream.read(), (byte) inputStream.read()).toString()));

            return new ConstantNode(Tags.DOUBLE, refs);
        }
    },
    CLASS(7) {
        public ConstantNode compute(InputStream inputStream) throws IOException {
            Vector<ResolveData> refs = new Vector<>();
            refs.add(new Ref(twoByteInInt((byte) inputStream.read(), (byte) inputStream.read()).toString()));
            return new ConstantNode(Tags.CLASS, refs);
        }
    },
    STRING(8) {
        public ConstantNode compute(InputStream inputStream) throws IOException {
            Vector<ResolveData> refs = new Vector<>();
            refs.add(new Ref(twoByteInInt((byte) inputStream.read(), (byte) inputStream.read()).toString()));
            return new ConstantNode(Tags.STRING, refs);
        }
    },
    FIELDREF(9){
        @Override
        public ConstantNode compute(InputStream inputStream) throws IOException {
            return defaultCompute(Tags.FIELDREF, inputStream);
        }
    },
    METHODREF(10){
        @Override
        public ConstantNode compute(InputStream inputStream) throws IOException {
            return defaultCompute(Tags.METHODREF, inputStream);
        }
    },
    INTERFACEMETHODREF(11){
        @Override
        public ConstantNode compute(InputStream inputStream) throws IOException {
            return defaultCompute(Tags.INTERFACEMETHODREF, inputStream);
        }
    },
    NAMEANDTYPE(12){
        @Override
        public ConstantNode compute(InputStream inputStream) throws IOException {
            return defaultCompute(Tags.NAMEANDTYPE, inputStream);
        }
    },
    METHODHANDLE(15) {
        public ConstantNode compute(InputStream inputStream) throws IOException {
            Vector<ResolveData> refs = new Vector<>();
            refs.add(new StaticData(String.valueOf((byte) inputStream.read())));
            refs.add(new StaticData(":"));
            refs.add(new Ref(twoByteInInt((byte) inputStream.read(), (byte) inputStream.read()).toString()));
            return new ConstantNode(Tags.METHODHANDLE, refs);
        }
    },
    METHODTYPE(16) {
        public ConstantNode compute(InputStream inputStream) throws IOException {
            Vector<ResolveData> refs = new Vector<>();
            refs.add(new Ref(twoByteInInt((byte) inputStream.read(), (byte) inputStream.read()).toString()));
            return new ConstantNode(Tags.METHODTYPE, refs);
        }
    },
    INVOKEDYNAMIC(18) {
        public ConstantNode compute(InputStream inputStream) throws IOException {
            Vector<ResolveData> refs = new Vector<>();
            refs.add(new StaticData(twoByteInInt((byte) inputStream.read(), (byte) inputStream.read()).toString()));
            refs.add(new StaticData(":"));
            refs.add(new Ref(twoByteInInt((byte) inputStream.read(), (byte) inputStream.read()).toString()));
            return new ConstantNode(Tags.INVOKEDYNAMIC, refs);
        }
    };

    int id;

    Tags(int id) {
        this.id = id;
    }

    public static Tags getById(int tag) throws Exception {
        switch (tag) {
            case 1:
                return Tags.UTF8;
            case 3:
                return Tags.INTEGER;
            case 5:
                return Tags.LONG;
            case 6:
                return Tags.DOUBLE;
            case 7:
                return Tags.CLASS;
            case 8:
                return Tags.STRING;
            case 9:
                return Tags.FIELDREF;
            case 10:
                return Tags.METHODREF;
            case 11:
                return Tags.INTERFACEMETHODREF;
            case 12:
                return Tags.NAMEANDTYPE;
            case 15:
                return Tags.METHODHANDLE;
            case 16:
                return Tags.METHODTYPE;
            case 18:
                return Tags.INVOKEDYNAMIC;
            default:
                throw new Exception();
        }
    }

    Integer twoByteInInt(byte b1, byte b2) {
        return ((0xFF & b1) << 8) | (0xFF & b2);
    }

    Integer fourByteInInt(byte b1, byte b2, byte b3, byte b4) {
        return ((0xFF & b1) << 24) | ((0xFF & b2) << 16) |
                ((0xFF & b3) << 8) | (0xFF & b4);
    }

    ConstantNode defaultCompute(Tags tag, InputStream inputStream) throws IOException{
        Vector<ResolveData> refs = new Vector<>();
        refs.add(new Ref(twoByteInInt((byte) inputStream.read(), (byte) inputStream.read()).toString()));
        refs.add(new StaticData("."));
        refs.add(new Ref(twoByteInInt((byte) inputStream.read(), (byte) inputStream.read()).toString()));
        return new ConstantNode(tag, refs);
    }

    abstract public ConstantNode compute(InputStream inputStream) throws IOException;
}