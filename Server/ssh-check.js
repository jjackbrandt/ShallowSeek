/**
 * SSH Configuration Checker
 * 
 * This tool checks SSH configuration on your system and 
 * provides guidance on setting up password authentication.
 */

const os = require('os');
const fs = require('fs');
const { execSync } = require('child_process');

console.log(`
╔══════════════════════════════════════════════╗
║                                              ║
║   SSH Configuration Checker for ShallowSeek  ║
║                                              ║
╚══════════════════════════════════════════════╝
`);

// System info
console.log('System Information:');
console.log(`Hostname: ${os.hostname()}`);
console.log(`Platform: ${os.platform()}`);
console.log(`Release: ${os.release()}`);

// Check if SSH is running
console.log('\nChecking if SSH server is running...');
let sshRunning = false;
try {
  if (os.platform() === 'linux' || os.platform() === 'darwin') {
    const sshStatus = execSync('ps -ef | grep sshd | grep -v grep').toString();
    sshRunning = sshStatus.length > 0;
    
    if (sshRunning) {
      console.log('✅ SSH server is running.');
      
      // Get SSH version
      try {
        const sshVersion = execSync('ssh -V 2>&1').toString();
        console.log(`SSH version: ${sshVersion.trim()}`);
      } catch (e) {
        console.log('Could not determine SSH version.');
      }
    } else {
      console.log('❌ SSH server does not appear to be running.');
      
      if (os.platform() === 'darwin') {
        console.log('\nTo enable SSH on macOS:');
        console.log('1. Go to System Preferences > Sharing');
        console.log('2. Check "Remote Login"');
      } else if (os.platform() === 'linux') {
        console.log('\nTo enable SSH on Linux:');
        console.log('1. Install OpenSSH server: sudo apt-get install openssh-server');
        console.log('2. Start SSH: sudo service ssh start');
        console.log('3. Enable SSH on boot: sudo systemctl enable ssh');
      }
    }
  } else if (os.platform() === 'win32') {
    try {
      const sshStatus = execSync('tasklist | findstr ssh').toString();
      sshRunning = sshStatus.includes('sshd.exe');
      if (sshRunning) {
        console.log('✅ SSH server is running.');
      } else {
        console.log('❌ SSH server does not appear to be running.');
        console.log('\nTo enable SSH on Windows:');
        console.log('1. Install OpenSSH Server from Optional Features');
        console.log('2. Start the service: net start sshd');
        console.log('3. Set to auto-start: sc config sshd start=auto');
      }
    } catch (e) {
      console.log('❌ SSH server does not appear to be running.');
    }
  }
} catch (e) {
  console.log('❌ SSH server does not appear to be running.');
  console.log(`Error: ${e.message}`);
}

// Check SSH config
console.log('\nChecking SSH server configuration...');
const sshdConfigPath = '/etc/ssh/sshd_config';

if (fs.existsSync(sshdConfigPath)) {
  try {
    console.log('✅ SSH config file exists at:', sshdConfigPath);
    
    // Check password authentication
    const sshdConfig = fs.readFileSync(sshdConfigPath, 'utf8');
    const passwordAuth = sshdConfig.match(/^PasswordAuthentication\s+(yes|no)/m);
    const pubkeyAuth = sshdConfig.match(/^PubkeyAuthentication\s+(yes|no)/m);
    
    if (passwordAuth) {
      const isPasswordEnabled = passwordAuth[1] === 'yes';
      console.log(`Password authentication is ${isPasswordEnabled ? '✅ enabled' : '❌ disabled'}`);
      
      if (!isPasswordEnabled) {
        console.log('\nTo enable password authentication:');
        console.log('1. Edit sshd_config: sudo nano /etc/ssh/sshd_config');
        console.log('2. Change/add the line: PasswordAuthentication yes');
        console.log('3. Restart SSH: sudo service ssh restart');
      }
    } else {
      console.log('Password authentication setting not found (default is typically yes)');
    }
    
    if (pubkeyAuth) {
      console.log(`Public key authentication is ${pubkeyAuth[1] === 'yes' ? '✅ enabled' : '❌ disabled'}`);
    } else {
      console.log('Public key authentication setting not found (default is typically yes)');
    }
    
  } catch (e) {
    console.log('❌ Could not read SSH config file.');
    console.log(`Error: ${e.message}`);
  }
} else {
  console.log('❌ SSH config file not found at the default location.');
  if (os.platform() === 'darwin') {
    console.log('On macOS, the config file should be at: /etc/ssh/sshd_config');
    console.log('You may need to enable Remote Login first.');
  } else if (os.platform() === 'linux') {
    console.log('On Linux, the config file should be at: /etc/ssh/sshd_config');
    console.log('You may need to install OpenSSH server first.');
  } else if (os.platform() === 'win32') {
    console.log('On Windows, the config file should be at: C:\\ProgramData\\ssh\\sshd_config');
    console.log('You may need to install OpenSSH server first.');
  }
}

// Print network interfaces
console.log('\nNetwork Interfaces:');
const networkInterfaces = os.networkInterfaces();
Object.keys(networkInterfaces).forEach(ifname => {
  const ifaces = networkInterfaces[ifname];
  ifaces.forEach(iface => {
    if (iface.family === 'IPv4' && !iface.internal) {
      console.log(`${ifname}: ${iface.address}`);
    }
  });
});

console.log('\nTroubleshooting Tips:');
console.log('1. Ensure SSH server is running');
console.log('2. Ensure password authentication is enabled in sshd_config');
console.log('3. Ensure your username and password are correct');
console.log('4. Ensure the server is accessible from your network (check firewalls)');
console.log('5. Test SSH connection manually: ssh username@host');
console.log('6. Use the correct IP address from the network interfaces list above');