require 'buildr/git_auto_version'
require 'buildr/gpg'
require 'buildr/single_intermediate_layout'
require 'buildr/top_level_generate_dir'
require 'buildr/gwt'
require 'buildr/jacoco'

GWT_EXAMPLES =
  {
  }

ZEMECKIS_TEST_OPTIONS =
  {
    'braincheck.environment' => 'development',
    'zemeckis.environment' => 'development'
  }

desc 'Zemeckis: A library to unify scheduling tasks'
define 'zemeckis' do
  project.group = 'org.realityforge.zemeckis'
  compile.options.source = '1.8'
  compile.options.target = '1.8'
  compile.options.lint = 'all,-processing,-serial'
  project.compile.options.warnings = true
  project.compile.options.other = %w(-Werror -Xmaxerrs 10000 -Xmaxwarns 10000)

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache_v2_license
  pom.add_github_project('realityforge/zemeckis')
  pom.add_developer('realityforge', 'Peter Donald')

  desc 'Zemeckis Core Library'
  define 'core' do
    project.processorpath << artifacts(:grim_processor, :javax_json)

    deps = artifacts(:javax_annotation,
                     :jsinterop_annotations,
                     :jsinterop_base,
                     :jetbrains_annotations,
                     :elemental2_core,
                     :elemental2_dom,
                     :elemental2_promise,
                     :braincheck,
                     :grim_annotations)
    pom.include_transitive_dependencies << deps
    pom.dependency_filter = Proc.new { |dep| dep[:scope].to_s != 'test' && deps.include?(dep[:artifact]) }

    compile.with deps

    gwt_enhance(project)

    package(:jar)
    package(:sources)
    package(:javadoc)

    test.using :testng
    test.compile.with :jdepend, :javax_json

    test.options[:properties] =
      ZEMECKIS_TEST_OPTIONS.merge('zemeckis.core.compile_target' => compile.target.to_s,
                                  'zemeckis.diagnostic_messages_file' => _('src/test/java/zemeckis/diagnostic_messages.json'))
    test.options[:java_args] = ['-ea']
  end

  desc 'Test Zemeckis API'
  define 'api-test' do
    test.compile.with :javax_annotation,
                      :javax_json,
                      :gir

    test.options[:properties] =
      ZEMECKIS_TEST_OPTIONS.merge(
        'zemeckis.api_test.store_api_diff' => ENV['STORE_API_DIFF'] == 'true',
        'zemeckis.prev.version' => ENV['PREVIOUS_PRODUCT_VERSION'],
        'zemeckis.prev.jar' => artifact("org.realityforge.zemeckis:zemeckis-core:jar:#{ENV['PREVIOUS_PRODUCT_VERSION'] || project.version}").to_s,
        'zemeckis.next.version' => ENV['PRODUCT_VERSION'],
        'zemeckis.next.jar' => project('core').package(:jar).to_s,
        'zemeckis.api_test.fixture_dir' => _('src/test/resources/fixtures').to_s,
        'zemeckis.revapi.jar' => artifact(:revapi_diff).to_s
      )
    test.options[:java_args] = ['-ea']
    test.using :testng

    test.compile.enhance do
      mkdir_p _('src/test/resources/fixtures')
      artifact("org.realityforge.zemeckis:zemeckis-core:jar:#{ENV['PREVIOUS_PRODUCT_VERSION']}").invoke
      project('core').package(:jar).invoke
      artifact(:revapi_diff).invoke
    end unless ENV['TEST'] == 'no' || ENV['PRODUCT_VERSION'].nil? || ENV['PREVIOUS_PRODUCT_VERSION'].nil?

    test.exclude '*ApiDiffTest' if ENV['PRODUCT_VERSION'].nil? || ENV['PREVIOUS_PRODUCT_VERSION'].nil?

    project.jacoco.enabled = false
  end

  desc 'Zemeckis Examples'
  define 'examples' do
    compile.with project('core').package(:jar),
                 project('core').compile.dependencies,
                 :gwt_user

    gwt_modules = {}
    GWT_EXAMPLES.each_pair do |gwt_module, path|
      gwt_modules["zemeckis.examples.dom.#{path}.#{gwt_module}"] = false
    end
    iml.add_gwt_facet(gwt_modules,
                      :settings => { :compilerMaxHeapSize => '1024' },
                      :gwt_dev_artifact => :gwt_dev)
    project.jacoco.enabled = false
  end

  doc.from(projects(%w(core))).
    using(:javadoc,
          :windowtitle => 'Zemeckis API Documentation',
          :linksource => true,
          :timestamp => false,
          :link => %w(https://docs.oracle.com/javase/8/docs/api)
    ).sourcepath << project('core').compile.sources

  cleanup_javadocs(project, 'zemeckis')

  iml.excluded_directories << project._('tmp')

  GWT_EXAMPLES.each_pair do |gwt_module, path|
    ipr.add_gwt_configuration(project,
                              :iml_name => 'examples',
                              :name => gwt_module,
                              :gwt_module => "zemeckis.examples.dom.#{path}.#{gwt_module}",
                              :start_javascript_debugger => false,
                              :open_in_browser => false,
                              :vm_parameters => '-Xmx2G',
                              :shell_parameters => "-strict -style PRETTY -XmethodNameDisplayMode FULL -nostartServer -incremental -codeServerPort 8889 -bindAddress 0.0.0.0 -deploy #{_(:generated, :gwt, 'deploy')} -extra #{_(:generated, :gwt, 'extra')} -war #{_(:generated, :gwt, 'war')}",
                              :launch_page => "http://127.0.0.1:8889/#{path}/index.html")
  end

  ipr.add_default_testng_configuration(:jvm_args => '-ea -Dbraincheck.environment=development -Dzemeckis.environment=development -Dzemeckis.output_fixture_data=false -Dzemeckis.fixture_dir=support/processor/src/test/resources -Dzemeckis.core.compile_target=target/zemeckis_core/idea/classes -Dzemeckis.diagnostic_messages_file=core/src/test/java/zemeckis/diagnostic_messages.json')

  ipr.add_testng_configuration('core',
                               :module => 'core',
                               :jvm_args => '-ea -Dbraincheck.environment=development -Dzemeckis.environment=development -Dzemeckis.output_fixture_data=false -Dzemeckis.core.compile_target=../target/zemeckis_core/idea/classes -Dzemeckis.check_diagnostic_messages=false -Dzemeckis.diagnostic_messages_file=src/test/java/zemeckis/diagnostic_messages.json')
  ipr.add_testng_configuration('core - update invariant messages',
                               :module => 'core',
                               :jvm_args => '-ea -Dbraincheck.environment=development -Dzemeckis.environment=development -Dzemeckis.output_fixture_data=true -Dzemeckis.core.compile_target=../target/zemeckis_core/idea/classes -Dzemeckis.check_diagnostic_messages=true -Dzemeckis.diagnostic_messages_file=src/test/java/zemeckis/diagnostic_messages.json')

  ipr.add_component_from_artifact(:idea_codestyle)
end

# Avoid uploading any packages except those we explicitly allow
Buildr.projects.each do |project|
  unless %w(zemeckis:core).include?(project.name)
    project.task('upload').actions.clear
  end
end
