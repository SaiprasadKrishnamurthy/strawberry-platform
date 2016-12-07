# -*- mode: ruby -*-
# vi: set ft=ruby :
Vagrant.configure(2) do |config|
    config.vm.define :strawberry do |strawberry_config|
        strawberry_config.vm.box = "jayunit100/centos7"
        strawberry_config.vm.host_name = "strawberry"
        strawberry_config.vm.network "private_network", ip:"192.168.100.222"
        strawberry_config.vm.network :forwarded_port, guest: 9999, host: 9999, auto_correct: true
        strawberry_config.vm.network :forwarded_port, guest: 5601, host: 5601, auto_correct: true
        strawberry_config.vm.network :forwarded_port, guest: 27017, host: 27017, auto_correct: true
        strawberry_config.vm.provision :shell, inline: "groupadd -f docker"
        strawberry_config.vm.provider :virtualbox do |vb|
            vb.memory = 6144
            vb.cpus = 4
        end
        strawberry_config.vm.provision :shell, inline: "sudo yum clean all && sudo yum -y install docker && curl -L https://github.com/docker/compose/releases/download/1.6.2/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
", run: "always"
    end
end
