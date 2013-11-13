package fr.xephi.authme.datasource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.api.API;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.settings.Settings;


public class FileDataSource implements DataSource {

    /* file layout:
     *
     * PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS:LASTPOSX:LASTPOSY:LASTPOSZ:LASTPOSWORLD
     *
     * Old but compatible:
     * PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS
     * PLAYERNAME:HASHSUM:IP
     * PLAYERNAME:HASHSUM
     *
     */
    private File source;

    public FileDataSource() throws IOException {
        source = new File(Settings.AUTH_FILE);
        source.createNewFile();
    }

    @Override
    public synchronized boolean isAuthAvailable(String user) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 1 && args[0].equals(user)) {
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return false;
    }

    @Override
    public synchronized boolean saveAuth(PlayerAuth auth) {
        if (isAuthAvailable(auth.getNickname())) {
            return false;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(source, true));
            bw.write(auth.getNickname() + ":" + auth.getHash() + ":" + auth.getIp() + ":" + auth.getLastLogin() + ":" + auth.getQuitLocX() + ":" + auth.getQuitLocY() + ":" + auth.getQuitLocZ() + ":" + auth.getWorld() + "\n");
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public synchronized boolean updatePassword(PlayerAuth auth) {
        if (!isAuthAvailable(auth.getNickname())) {
            return false;
        }
        PlayerAuth newAuth = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args[0].equals(auth.getNickname())) {
                	switch (args.length) {
                		case 4: {
                			newAuth = new PlayerAuth(args[0], auth.getHash(), args[2], Long.parseLong(args[3]), 0, 0, 0, "world", "your@email.com", API.getPlayerRealName(args[0]));
                			break;
                		}
                		case 7: {
                			newAuth = new PlayerAuth(args[0], auth.getHash(), args[2], Long.parseLong(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), "world", "your@email.com", API.getPlayerRealName(args[0]));
                			break;
                		}
                		case 8: {
                			newAuth = new PlayerAuth(args[0], auth.getHash(), args[2], Long.parseLong(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), args[7], "your@email.com", API.getPlayerRealName(args[0]));
                			break;
                		}
                		default: {
                			newAuth = new PlayerAuth(args[0], auth.getHash(), args[2], 0, 0, 0, 0, "world", "your@email.com", API.getPlayerRealName(args[0]));
                			break;
                		}
                	}
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        removeAuth(auth.getNickname());
        saveAuth(newAuth);
        return true;
    }

    @Override
    public boolean updateSession(PlayerAuth auth) {
        if (!isAuthAvailable(auth.getNickname())) {
            return false;
        }
        PlayerAuth newAuth = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args[0].equals(auth.getNickname())) {
                    switch (args.length) {
                    	case 4: {
                    		newAuth = new PlayerAuth(args[0], args[1], auth.getIp(), auth.getLastLogin(), 0, 0, 0, "world", "your@email.com", API.getPlayerRealName(args[0]));
                    		break;
                    	}
                    	case 7: {
                    		newAuth = new PlayerAuth(args[0], args[1], auth.getIp(), auth.getLastLogin(), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), "world", "your@email.com", API.getPlayerRealName(args[0]));
                    		break;
                    	}
                    	case 8: {
                    		newAuth = new PlayerAuth(args[0], args[1], auth.getIp(), auth.getLastLogin(), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), args[7], "your@email.com", API.getPlayerRealName(args[0]));
                    		break;
                    	}
                    	default: {
                    		newAuth = new PlayerAuth(args[0], args[1], auth.getIp(), auth.getLastLogin(), 0, 0, 0, "world", "your@email.com", API.getPlayerRealName(args[0]));
                    		break;
                    	}
                    }
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        removeAuth(auth.getNickname());
        saveAuth(newAuth);
        return true;
    }

   @Override
   public boolean updateQuitLoc(PlayerAuth auth) {
       if (!isAuthAvailable(auth.getNickname())) {
            return false;
        }
        PlayerAuth newAuth = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args[0].equals(auth.getNickname())) {
                    newAuth = new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), auth.getQuitLocX(), auth.getQuitLocY(), auth.getQuitLocZ(), auth.getWorld(), "your@email.com", API.getPlayerRealName(args[0]));
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        removeAuth(auth.getNickname());
        saveAuth(newAuth);
        return true;
    }

    @Override
    public int getIps(String ip) {
        BufferedReader br = null;
        int countIp = 0;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 3 && args[2].equals(ip)) {
                    countIp++;
                }
            }
            return countIp;
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return 0;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return 0;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        } 
    }

    @Override
    public int purgeDatabase(long until) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> lines = new ArrayList<String>();
        int cleared = 0;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length >= 4) {
                    if (Long.parseLong(args[3]) >= until) {
                        lines.add(line);
                        continue;
                    }
                }
                cleared++;
            }
            bw = new BufferedWriter(new FileWriter(source));
            for (String l : lines) {
                bw.write(l + "\n");
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return cleared;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return cleared;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return cleared;
    }

    @Override
    public List<String> autoPurgeDatabase(long until) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> lines = new ArrayList<String>();
        List<String> cleared = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length >= 4) {
                    if (Long.parseLong(args[3]) >= until) {
                        lines.add(line);
                        continue;
                    } else {
                    	cleared.add(args[0]);
                    }
                }

            }
            bw = new BufferedWriter(new FileWriter(source));
            for (String l : lines) {
                bw.write(l + "\n");
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return cleared;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return cleared;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return cleared;
    }

    @Override
    public synchronized boolean removeAuth(String user) {
        if (!isAuthAvailable(user)) {
            return false;
        }
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 1 && !args[0].equals(user)) {
                    lines.add(line);
                }
            }
            bw = new BufferedWriter(new FileWriter(source));
            for (String l : lines) {
                bw.write(l + "\n");
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public synchronized PlayerAuth getAuth(String user) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args[0].equals(user)) {
                    switch (args.length) {
                        case 2:
                            return new PlayerAuth(args[0], args[1], "198.18.0.1", 0, "your@email.com", API.getPlayerRealName(args[0]));
                        case 3:
                            return new PlayerAuth(args[0], args[1], args[2], 0, "your@email.com",  API.getPlayerRealName(args[0]));
                        case 4:
                            return new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), "your@email.com",  API.getPlayerRealName(args[0]));
                        case 7:
                            return new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), "unavailableworld", "your@email.com", API.getPlayerRealName(args[0]));
                        case 8:
                        	return new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), args[7], "your@email.com", API.getPlayerRealName(args[0]));
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return null;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return null;
    }

    @Override
    public synchronized void close() {
    }

    @Override
    public void reload() {
    }

	@Override
	public boolean updateEmail(PlayerAuth auth) {
		return false;
	}

	@Override
	public boolean updateSalt(PlayerAuth auth) {
		return false;
	}

	@Override
	public List<String> getAllAuthsByName(PlayerAuth auth) {
        BufferedReader br = null;
        List<String> countIp = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 3 && args[2].equals(auth.getIp())) {
                    countIp.add(args[0]);
                }
            }
            return countIp;
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        } 
	}

	@Override
	public List<String> getAllAuthsByIp(String ip) {
        BufferedReader br = null;
        List<String> countIp = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 3 && args[2].equals(ip)) {
                    countIp.add(args[0]);
                }
            }
            return countIp;
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        } 
	}

	@Override
	public List<String> getAllAuthsByEmail(String email) {
		return new ArrayList<String>();
	}

	@Override
	public void purgeBanned(List<String> banned) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                try {
                	if (banned.contains(args[0])) {
                		lines.add(line);
                	}
                } catch (NullPointerException npe) {}
                catch (ArrayIndexOutOfBoundsException aioobe) {}
            }
            bw = new BufferedWriter(new FileWriter(source));
            for (String l : lines) {
                bw.write(l + "\n");
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return;
	}

}
