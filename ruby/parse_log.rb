local_filename = "/home/kaan/Desktop/restrict-maven-plugin.txt"

File.open(local_filename, 'r') do |f|
  paket = nil
  while line = f.gets
    if line.start_with?("Restricted access from")
      tokens = line.split(/\(|\)/)
      paket_suan = tokens[1][tokens[1].rindex(/\\/)+1..-1]
      if paket_suan != paket
        paket = paket_suan
        puts "<br\><br\>"
        puts "</ul>" if (paket != nil)
        puts "<h2>#{paket}:</h2><ul>"
      end
      puts "<li>#{tokens[2][1..-1]}</li>"
    end
  end
end

