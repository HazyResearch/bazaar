from fabric.api import *
from fabric.tasks import execute

def get_platform():
    with hide('everything'):
        return run("uname -s")

def is_installed(cmd):
    with settings(warn_only=True):
        with hide('everything'):
            result = run('command -v ' + cmd)
            if result.return_code == 0:
                return True
            else: 
                return False
    
def install_git():
    platform = get_platform()
    with hide():
        if platform == 'Darwin':
            run('sudo brew -y install git')
        if platform == 'Linux':
            run('sudo apt-get -y install git')

@task 
@parallel
def install():
    platform = get_platform()
    #if not is_installed('git'):
    #    print('Node ' + env.host + ' does not have git installed!')
    #    install_git()
    #r = run('git clone https://github.com/hazyresearch/bazaar.git')
    #if not r.return_code == 0:
    #    print('ERROR. Aborting')
    #    sys.exit()
    run('mkdir -p ~/parser')
    put(local_path='../parser', remote_path='~')
    r = run('cd ~/parser; chmod +x *.sh sbt/sbt; ./setup.sh')
    if not r.return_code == 0:
        print('ERROR. Aborting')
        sys.exit()

@task
def split():
    local('mkdir -p ~/segments')
    local('split -a 5 -l 2 test/INPUT segments/')

@task
@parallel
def copy():
    run('mkdir -p ~/segments')
    num_machines = len(env.all_hosts)
    machine = env.all_hosts.index(env.host)

    output = local('find segments -type f', capture=True)
    files = output.split('\n')
    for f in files:
        file_num = hash(f) 
        file_machine = file_num % num_machines
        if file_machine == machine:
            print "put %s on machine %d" % (f, file_machine)
            put(local_path=f, remote_path='~/segments')

@task
@parallel
def echo():
    run('echo "$HOSTNAME"')

@task
@parallel
def parse():
    run('cd ~/parser; chmod +x *.sh; ./run_parallel.sh')
    run('echo "$HOSTNAME"')
    pass


@parallel
def collect():
    # collect all files ending in .parsed and .failed
    output = run('find segments -type f -name *.*')
    files = output.split('\n')
    for f in files:
        path = '~/' + f
        get(local_path='segments', remote_path=path)

def merge():
    local('rm result')
    local('find segments -type f -name *.parsed -print0 | xargs cat $0 >> result')
    print('Done. You can now load the result into your database.')

def all():
    pass

def read_hosts():
    env.hosts = open('HOSTS', 'r').readlines()
    env.user = "ubuntu"
    env.key_filename = "./ssh/bazaar.key"

read_hosts()

