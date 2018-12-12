# Change Log
All notable changes to this project will be documented in this file.

The format of the file is based on a template from [Keep a Changelog](http://keepachangelog.com/).

## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

## [2.7.9] - 2018-12-07
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.9

### Added
- extended ALDOperatorCollection to allow for re-running operators with unchanged configuration

### Fixed
- fixed handling of event queue in workflow event handling towards larger thread-safety

## [2.7.8] - 2018-09-03
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.8

### Added
- ALDOperatorCollection: new framework to easily manage, i.e. configure and run, groups of operators
- ALDOperatorControllable: new wrapper class for control status to ease propagation of status to sub-routines

## [2.7.7] - 2018-05-18
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.7

## [2.7.6] - 2018-03-23
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.6

### Changed
- introduced Alida parent POM file in Maven project configuration
- ALDFileDirectoryDataIOSwing: leaving non-required parameters blank upon update now

## [2.7.5.1] - 2018-01-31
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.5.1

### Changed
- ALDWorkflowEvent: renamed method to access info object

## [2.7.5] - 2017-11-10
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.5

### Added
- XML provider for Java data type EnumSet

### Fixed
- fixed some data I/O providers to suppress value events during internal updates of GUI elements
- updated XML schemes for storing operator parameters, visibility is now correctly considered

## [2.7.4] - 2017-07-29
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.4

### Added
- New data I/O provider for Java data type EnumSet

### Changed
- Updated to JGraphX 3.4.1.3

### Fixed
- ALDNativeArrayXXDataIOSwing: loading table data for 1D/2D arrays from file now displays errors if data format does not fit instead of just failing silently
- ALDVersionProviderGit: properly initializing provider even if environment variable is not set (issue #1)

## [2.7.3] - 2016-12-14
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.3

## [2.7.2] - 2016-05-20
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.2

## [2.7.1] - 2016-05-20
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7.1

## [2.7] - 2016-03-15
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.7

## [2.6.3] - 2016-03-08
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.6.3

## [2.6.2] - 2016-03-03
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.6.2

## [2.6.1] - 2015-09-23
Birgit Moeller - <birgit.moeller@informatik.uni-halle.de>
- Released Alida 2.6.1





