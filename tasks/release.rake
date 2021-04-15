require 'buildr/release_tool.rb')

Buildr::ReleaseTool.define_release_task do |t|
  t.extract_version_from_changelog
  t.zapwhite
  t.ensure_git_clean
  t.verify_no_todo
  t.cleanup_staging
  t.build(:additional_tasks => 'do_test_api_diff')
  t.patch_changelog('realityforge/zemeckis', :api_diff_directory => "#{WORKSPACE_DIR}/api-test")
  t.tag_project
  t.stage_release(:release_to => { :url => 'https://stocksoftware.jfrog.io/stocksoftware/staging', :username => ENV['STAGING_USERNAME'], :password => ENV['STAGING_PASSWORD'] })
  t.maven_central_publish(:additional_tasks => 'site:publish_if_tagged')
  t.patch_changelog_post_release
  t.push_changes
  t.github_release('realityforge/zemeckis')
end
