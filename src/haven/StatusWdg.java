package haven;

import javax.net.ssl.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusWdg extends Widget {

    public static String username;
    public static String pass;

    private static ThreadGroup tg = new ThreadGroup("StatusUpdaterThreadGroup");
    private String statusupdaterthreadname = "StatusUpdater";

    private Tex hearthlingsplaying = Text.render("Players: ?", Color.WHITE).tex();
    private Tex pingtime = Text.render("Ping: ?", Color.WHITE).tex();
    private Tex accountstatus = Text.render("Account status: ?", Color.WHITE).tex();

    private static SSLSocketFactory sslfactory;

    static {
        InputStream crt = null;
        try {
            KeyStore keystore  = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null);
            crt = Resource.class.getResourceAsStream("websrv.crt");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            keystore.setCertificateEntry("havenandhearth", cf.generateCertificate(crt));
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);
            sslfactory = ctx.getSocketFactory();
        } catch (CertificateException ce) {
            ce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        } catch (KeyStoreException kse) {
            kse.printStackTrace();
        } catch (KeyManagementException kme) {
            kme.printStackTrace();
        } finally {
            try {
                if (crt != null)
                    crt.close();
            } catch (IOException ioe) {
            }
        }
    }

    public StatusWdg() {
        synchronized (StatusWdg.class) {
            Thread[] activethreads = new Thread[tg.activeCount()];
            int count = tg.enumerate(activethreads);
            for (int i = 0; i < count; i++) {
                if (activethreads[i].getName().equals(this.statusupdaterthreadname)) {
                    activethreads[i].interrupt();
                    try {
                        activethreads[i].join();
                    } catch (InterruptedException ex) {
                        // NOP
                    }
                }
            }

            startupdaterthread();
        }
    }

    private void updatehearthlingscount() {
        String hearthlingscount = "?";

        String mainpagecontent = geturlcontent("https://www.havenandhearth.com/portal/");
        if (!mainpagecontent.isEmpty())
            hearthlingscount = StringExtensions.getstringbetween(mainpagecontent, "There are", "hearthlings playing").trim();

        if (hearthlingscount.isEmpty())
            hearthlingscount = "?";

        synchronized (StatusWdg.class) {
            hearthlingsplaying = Text.render(String.format("Players: %s", hearthlingscount), Color.WHITE).tex();
        }
    }

    private void updatepingtime() {
        String ping = "?";

        java.util.List<String> command = new ArrayList<>();
        command.add("ping");
        command.add(Config.iswindows ? "-n" : "-c");
        command.add("1");
        command.add("game.havenandhearth.com");

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            BufferedReader standardOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String output = "";
            String line;
            while ((line = standardOutput.readLine()) != null) {
                output += line;
            }

            Pattern pattern;
            if (Config.iswindows) {
                // Reply from 87.245.198.59: bytes=32 time=2ms TTL=53
                // Ответ от 213.180.204.3: число байт=32 время=12мс TTL=55
                pattern = Pattern.compile(".+?=32 .+?=(\\d+).*? TTL=.+");
            } else {
                // 64 bytes from ansgar.seatribe.se (213.239.201.139): icmp_seq=1 ttl=47 time=71.4 ms
                pattern = Pattern.compile(".+?time=(\\d+\\.?\\d*) ms");
            }
            Matcher matcher = pattern.matcher(output);
            while (matcher.find()) {
                ping = matcher.group(1);
            }
        } catch (IOException ex) {
            // NOP
        }

        if (ping.isEmpty()) {
            ping = "?";
        }

        synchronized (this) {
            pingtime = Text.render(String.format("Ping: %s ms", ping), Color.WHITE).tex();
        }
    }

    private boolean mklogin() {
        if (sslfactory == null)
            return false;
        try {
            URL url = new URL("https://www.havenandhearth.com/portal/sec/login");
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setSSLSocketFactory(sslfactory);

            Map<String,Object> params = new LinkedHashMap<>();
            params.put("username", username);
            params.put("password", pass);

            StringBuilder postdata = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postdata.length() != 0)
                    postdata.append('&');
                postdata.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postdata.append('=');
                postdata.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postdata.toString().length()));

            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(postdata.toString());
            wr.flush();
            wr.close();

            int responsecode = conn.getResponseCode();
            if (responsecode != HttpsURLConnection.HTTP_OK)
                return false;
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    private void updateaccountstatus() {
        String status = "?";

        int retriescount = 0;
        while (retriescount < 2) {
            String profilepagecontent = geturlcontent("https://www.havenandhearth.com/portal/profile");
            status = StringExtensions.removehtmltags(StringExtensions.getstringbetween(profilepagecontent, "Account status:", "(All times")).trim();
            if (status.isEmpty()) {
                mklogin();
                ++retriescount;
                continue;
            }

            break;
        }

        if (status.isEmpty())
            status = "?";

        synchronized (StatusWdg.class) {
            accountstatus = Text.render(String.format("Account status: %s", status), Color.WHITE).tex();
        }
    }

    private void startupdaterthread() {
        Thread statusupdaterthread = new Thread(tg, new Runnable() {
            public void run() {
                CookieHandler.setDefault(new CookieManager());

                mklogin();
                if (Thread.interrupted())
                    return;

                while (true) {
                    updatehearthlingscount();
                    if (Thread.interrupted())
                        return;

                    updatepingtime();
                    if (Thread.interrupted())
                        return;

                    updateaccountstatus();
                    if (Thread.interrupted())
                        return;

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        }, this.statusupdaterthreadname);
        statusupdaterthread.start();
    }

    private String geturlcontent(String url) {
        if (sslfactory == null)
            return "";
        URL url_;
        InputStream is = null;
        BufferedReader br;
        String urlcontent = "";

        try {
            url_ = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection)url_.openConnection();
            conn.setSSLSocketFactory(sslfactory);
            is = conn.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                urlcontent += line;
            }
        } catch (SocketException se) {
            // don't print socket exceptions when network is unreachable to prevent console spamming on bad connections
            if (!se.getMessage().equals("Network is unreachable"))
                se.printStackTrace();
            return "";
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            return "";
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return "";
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ioe) {
                // NOP
            }
        }

        return urlcontent;
    }

    @Override
    public void draw(GOut g) {
        synchronized (StatusWdg.class) {
            java.util.List<Tex> texturesToDisplay = new ArrayList<>(3);
            texturesToDisplay.add(this.hearthlingsplaying);
            texturesToDisplay.add(this.pingtime);
            texturesToDisplay.add(this.accountstatus);

            int requiredwidth = 0;
            int requiredheight = 0;
            int y = 0;
            for (Tex textodisplay : texturesToDisplay) {
                g.image(textodisplay, new Coord(0, y));

                if (textodisplay.sz().x > requiredwidth)
                    requiredwidth = textodisplay.sz().x;
                y += textodisplay.sz().y;
            }

            requiredheight = y;
            this.sz = new Coord(requiredwidth, requiredheight);
        }
    }
}
