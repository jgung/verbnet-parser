import PropTypes from 'prop-types';
import React, { Component } from 'react';
import {
  Button,
  Container,
  Grid,
  Header,
  Icon,
  Image,
  List,
  Menu,
  Responsive,
  Segment,
  Sidebar,
  Visibility,
} from 'semantic-ui-react';
import './App.css';
import clearLogo from './clear.png';
import ParsingDemo from './components/ParsingDemo';

const getWidth = () => {
  const isSSR = typeof window === 'undefined';

  return isSSR ? Responsive.onlyTablet.minWidth : window.innerWidth;
};

const HomepageHeading = ({ mobile }) => (
  <Container text>
    <Header
      as="h1"
      textAlign="center"
      content="VerbNet Parser"
      inverted
      style={{
        fontSize: mobile ? '2em' : '4em',
        fontWeight: 'normal',
        marginBottom: 0,
        marginTop: mobile ? '1.5em' : '3em',
      }}
    />
    <ParsingDemo />
  </Container>
);

HomepageHeading.propTypes = {
  mobile: PropTypes.bool,
};

HomepageHeading.defaultProps = {
  mobile: false,
};

class DesktopContainer extends Component {
    state = {};

    hideFixedMenu = () => this.setState({ fixed: false });

    showFixedMenu = () => this.setState({ fixed: true });

    render() {
      const { children } = this.props;
      const { fixed } = this.state;

      return (
        <Responsive getWidth={getWidth} minWidth={Responsive.onlyTablet.minWidth}>
          <Visibility
            once={false}
            onBottomPassed={this.showFixedMenu}
            onBottomPassedReverse={this.hideFixedMenu}
          >
            <Segment
              inverted
              textAlign="left"
              style={{ minHeight: 700, padding: '1em 0em' }}
              vertical
            >
              <Menu
                fixed={fixed ? 'top' : null}
                inverted={!fixed}
                pointing={!fixed}
                secondary={!fixed}
                size="large"
              >
                <Container>
                  <Menu.Item as="a" active>Demo</Menu.Item>
                  <Menu.Item as="a" href="https://uvi.colorado.edu/">VerbNet</Menu.Item>
                  <Menu.Item as="a" href="https://www.colorado.edu/lab/clear/">CLEAR @ Colorado</Menu.Item>
                  <Menu.Item position="right" as="a" href="https://github.com/jgung/verbnet-parser">
                    <Icon name="github" />
                    {' '}
                    GitHub
                  </Menu.Item>
                </Container>
              </Menu>
              <HomepageHeading />
            </Segment>
          </Visibility>
          {children}
        </Responsive>
      );
    }
}

DesktopContainer.propTypes = {
  children: PropTypes.node,
};

class MobileContainer extends Component {
    state = {};

    handleSidebarHide = () => this.setState({ sidebarOpened: false });

    handleToggle = () => this.setState({ sidebarOpened: true });

    render() {
      const { children } = this.props;
      const { sidebarOpened } = this.state;

      return (
        <Responsive
          as={Sidebar.Pushable}
          getWidth={getWidth}
          maxWidth={Responsive.onlyMobile.maxWidth}
        >
          <Sidebar
            as={Menu}
            animation="push"
            inverted
            onHide={this.handleSidebarHide}
            vertical
            visible={sidebarOpened}
          >
            <Menu.Item as="a" active>Demo</Menu.Item>
            <Menu.Item as="a" href="https://uvi.colorado.edu/">VerbNet</Menu.Item>
            <Menu.Item as="a" href="https://www.colorado.edu/lab/clear/">CLEAR @ Colorado</Menu.Item>
          </Sidebar>

          <Sidebar.Pusher dimmed={sidebarOpened}>
            <Segment
              inverted
              textAlign="center"
              style={{ minHeight: 350, padding: '1em 0em' }}
              vertical
            >
              <Container>
                <Menu inverted pointing secondary size="large">
                  <Menu.Item onClick={this.handleToggle}>
                    <Icon name="sidebar" />
                  </Menu.Item>
                  <Menu.Item position="right" as="a" href="https://github.com/jgung/verbnet-parser">
                    <Icon name="github" />
                    {' '}
                    GitHub
                  </Menu.Item>
                </Menu>
              </Container>
              <HomepageHeading mobile />
            </Segment>
            {children}
          </Sidebar.Pusher>
        </Responsive>
      );
    }
}

MobileContainer.propTypes = {
  children: PropTypes.node,
};

const ResponsiveContainer = ({ children }) => (
  <div>
    <DesktopContainer>{children}</DesktopContainer>
    <MobileContainer>{children}</MobileContainer>
  </div>
);

ResponsiveContainer.propTypes = {
  children: PropTypes.node,
};

class App extends Component {
  render() {
    return (
      <ResponsiveContainer>
        <Segment style={{ padding: '8em 0em' }} vertical>
          <Grid container stackable verticalAlign="middle">
            <Grid.Row>
              <Grid.Column width={8}>
                <Header as="h3" style={{ fontSize: '2em' }}>
                                    A Class-Based Verb Lexicon
                </Header>
                <p style={{ fontSize: '1.33em' }}>
                                    VerbNet provides a hierarchical, domain-independent broad-coverage verb lexicon with mappings to
                                    other lexical resources.
                </p>
                <Header as="h3" style={{ fontSize: '2em' }}>
                                    329 verb classes and over 270 subclasses
                </Header>
                <p style={{ fontSize: '1.33em' }}>
                                    6,791 unique verb senses with corresponding thematic roles and entailed open-domain semantic
                                    predicates.
                </p>
              </Grid.Column>
              <Grid.Column floated="right" width={6}>
                <Image href="https://www.colorado.edu/lab/clear/" rounded size="large" src={clearLogo} />
              </Grid.Column>
            </Grid.Row>
            <Grid.Row>
              <Grid.Column textAlign="center">
                <Button primary size="huge" href="https://uvi.colorado.edu/">Check it out!</Button>
              </Grid.Column>
            </Grid.Row>
          </Grid>
        </Segment>

        <Segment style={{ padding: '0em' }} vertical>
          <Grid celled="internally" columns="equal" stackable>
            <Grid.Row textAlign="center">
              <Grid.Column style={{ paddingBottom: '5em', paddingTop: '5em' }}>
                <Header as="h3" style={{ fontSize: '2em' }}>
                                    Over 150 open-domain semantic predicates
                </Header>
                <p style={{ fontSize: '1.33em' }}>
                                    Predicates provide shared event semantics across classes grounded in a fixed set of semantic
                                    primitives.
                </p>
              </Grid.Column>
              <Grid.Column style={{ paddingBottom: '5em', paddingTop: '5em' }}>
                <Header as="h3" style={{ fontSize: '2em' }}>
                                    2,168 syntactic frames with associated semantic predicates
                </Header>
                <p style={{ fontSize: '1.33em' }}>
                                    Each class contains a set of syntactic descriptions, or syntactic frames, depicting the
                                    possible surface realizations of argument structures.
                </p>
              </Grid.Column>
            </Grid.Row>
          </Grid>
        </Segment>

        <Segment style={{ padding: '8em 0em' }} vertical>
          <Container text>
            <Header as="h3" style={{ fontSize: '2em' }}>
                            Linked to WordNet, PropBank, and FrameNet
            </Header>
            <List size="large">
              <List.Item>
                <List.Header as="a" href="https://wordnet.princeton.edu/">WordNet</List.Header>
                <List.Description>
                    A lexicon that describes semantic relationships (such as synonymy and hyperonymy) between
                    individual words.
                </List.Description>
              </List.Item>
              <List.Item>
                <List.Header as="a" href="https://propbank.github.io/">PropBank</List.Header>
                <List.Description>
                    A corpus of one million words of English text, annotated with argument role labels for verbs;
                    and a lexicon defining those argument roles on a per-verb basis.
                </List.Description>
              </List.Item>
              <List.Item>
                <List.Header as="a" href="https://framenet.icsi.berkeley.edu/fndrupal/">FrameNet</List.Header>
                <List.Description>
                                    A lexicon based on frame semantics.
                </List.Description>
              </List.Item>
            </List>
            <Button as="a" basic primary size="large" href="https://verbs.colorado.edu/semlink/">
                            Read More
            </Button>
          </Container>
        </Segment>

        <Segment inverted vertical style={{ padding: '5em 0em' }}>
          <Container>
            <Grid divided inverted stackable>
              <Grid.Row>
                <Grid.Column width={3}>
                  <Header inverted as="h4" content="VerbNet Resources" />
                  <List link inverted>
                    <List.Item as="a" href="http://verbs.colorado.edu/verb-index/vn3.3/">
Unified Verb
                                            Index
                    </List.Item>
                    <List.Item as="a" href="https://uvi.colorado.edu/">Unified Verb Index 2.0</List.Item>
                    <List.Item as="a" href="http://verbs.colorado.edu/semlink/">SemLink</List.Item>
                    <List.Item as="a" href="https://github.com/kevincstowe/verbnet">GitHub</List.Item>
                  </List>
                </Grid.Column>
                <Grid.Column width={3}>
                  <Header inverted as="h4" content="Applications" />
                  <List link inverted>
                    <List.Item as="a" href="https://github.com/jgung/verbnet-parser">
VerbNet Semantic
                                            Parsing
                    </List.Item>
                    <List.Item as="a" href="https://github.com/clearwsd/clearwsd">
VerbNet Sense
                                            Disambiguation
                    </List.Item>
                  </List>
                </Grid.Column>
                <Grid.Column width={7}>
                  <Header as="h4" inverted>
                                        Contact Us
                  </Header>
                  <List link inverted>
                    <List.Item as="a" href="https://www.colorado.edu/lab/clear/contact-us">
Center for
                                            Computational Language and Education Research
                    </List.Item>
                  </List>
                </Grid.Column>
              </Grid.Row>
            </Grid>
          </Container>
        </Segment>
      </ResponsiveContainer>
    );
  }
}

export default App;
