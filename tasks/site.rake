require File.expand_path(File.dirname(__FILE__) + '/util')

SITE_DIR = "#{WORKSPACE_DIR}/target/zemeckis/doc"

desc 'Publish the website'
task 'site:publish' => 'doc' do
  origin_url = 'https://github.com/realityforge/zemeckis.git'

  travis_build_number = ENV['TRAVIS_BUILD_NUMBER']
  if travis_build_number
    origin_url = origin_url.gsub('https://github.com/', 'git@github.com:')
  end

  local_dir = "#{WORKSPACE_DIR}/target/remote_site"
  rm_rf local_dir

  sh "git clone -b gh-pages --depth 1 #{origin_url} #{local_dir}"

  in_dir(local_dir) do
    message = "Publishing docs#{travis_build_number.nil? ? '' : " - Travis build: #{travis_build_number}"}"

    sh 'git rm -rf .'

    cp_r Dir["#{SITE_DIR}/*"], "#{local_dir}/."
    cp Dir["#{SITE_DIR}/*.*"], local_dir
    sh 'git add . -f'
    puts `git commit -m "#{message}"`
    if 0 == $?.exitstatus
      sh 'git push origin gh-pages'
    end
  end
end

desc 'Publish website iff current HEAD is a tag on blessed branch'
task 'site:publish_if_tagged' do
  candidate_branches = %w(master)
  tag = get_head_tag_if_any
  if tag.nil?
    puts 'Current HEAD is not a tag. Skipping site:publish step.'
  else
    puts "Current HEAD is a tag: #{tag}"
    if is_tag_on_candidate_branches?(tag, candidate_branches)
      task('site:publish').invoke
    end
  end
end

def get_head_tag_if_any
  version = `git describe --exact-match --tags 2>&1`
  if 0 == $?.exitstatus && version =~ /^v[0-9]/ && (ENV['TRAVIS_BUILD_ID'].nil? || ENV['TRAVIS_TAG'].to_s != '')
    version.strip
  else
    nil
  end
end

def is_tag_on_branch?(tag, branch)
  output = `git tag --merged #{branch} 2>&1`
  tags = output.split
  tags.include?(tag)
end

def is_tag_on_candidate_branches?(tag, branches)
  sh 'git fetch origin'
  branches.each do |branch|
    if is_tag_on_branch?(tag, branch)
      puts "Tag #{tag} is on branch: #{branch}"
      return true
    elsif is_tag_on_branch?(tag, "origin/#{branch}")
      puts "Tag #{tag} is on branch: origin/#{branch}"
      return true
    else
      puts "Tag #{tag} is not on branches: #{branch} or origin/#{branch}"
    end
  end
  false
end
