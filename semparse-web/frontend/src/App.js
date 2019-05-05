import PropTypes from 'prop-types';
import React, { Component } from 'react';
import {
  Button,
  Container,
  Grid,
  Header,
  Image,
  List,
  Menu,
  Segment,
} from 'semantic-ui-react';
import './App.css';
import clearLogo from './clear.png';
import DesktopContainer from './components/DesktopContainer';
import MobileContainer from './components/MobileContainer';


const ResponsiveContainer = ({ children, menuItems }) => (
  <div>
    <DesktopContainer menuItems={menuItems}>{children}</DesktopContainer>
    <MobileContainer menuItems={menuItems}>{children}</MobileContainer>
  </div>
);

ResponsiveContainer.propTypes = {
  children: PropTypes.node.isRequired,
  menuItems: PropTypes.arrayOf(PropTypes.node).isRequired,
};

class App extends Component {
    renderHeader = header => <Header key={header} as="h3" style={{ fontSize: '2em' }} content={header} />;

    renderTextContent = ({ header, text }) => [
      this.renderHeader(header),
      <p key={text} style={{ fontSize: '1.33em' }}>{text}</p>,
    ];

    renderListItem = ({ link, name, description }) => (
      <List.Item>
        <List.Header as="a" href={link} content={name} />
        <List.Description content={description} />
      </List.Item>
    );

    render() {
      const menuItems = [
        <Menu.Item key="Demo" as="a" active>Demo</Menu.Item>,
        <Menu.Item key="VerbNet" as="a" href="https://uvi.colorado.edu/">VerbNet</Menu.Item>,
        <Menu.Item key="CLEAR" as="a" href="https://www.colorado.edu/lab/clear/">CLEAR @ Colorado</Menu.Item>,
      ];

      const content1 = {
        header: 'A Class-Based Verb Lexicon',
        text: 'VerbNet provides a hierarchical, domain-independent broad-coverage verb lexicon with mappings to other lexical resources.',
      };
      const content2 = {
        header: '329 verb classes and over 270 subclasses',
        text: '6,791 unique verb senses with corresponding thematic roles and entailed open-domain semantic predicates.',
      };
      const content3 = {
        header: 'Over 150 open-domain semantic predicates',
        text: 'Predicates provide shared event semantics across classes grounded in a fixed set of semantic primitives.',
      };
      const content4 = {
        header: '2,168 syntactic frames with associated semantic predicates',
        text: 'Each class contains a set of syntactic descriptions, or syntactic frames, depicting the possible surface realizations of argument structures.',
      };

      const link1 = {
        link: 'https://wordnet.princeton.edu/',
        name: 'WordNet',
        description: 'A lexicon that describes semantic relationships (such as synonymy and hyperonymy) between individual words.',
      };
      const link2 = {
        link: 'https://propbank.github.io/',
        name: 'PropBank',
        description: 'A corpus of one million words of English text, annotated with argument role labels for verbs; and a lexicon defining those argument roles on a per-verb basis.',
      };
      const link3 = {
        link: 'https://framenet.icsi.berkeley.edu/fndrupal/',
        name: 'FrameNet',
        description: 'A lexicon based on frame semantics.',
      };

      return (
        <ResponsiveContainer menuItems={menuItems}>

          <Segment style={{ padding: '8em 0em' }} vertical>
            <Grid container stackable verticalAlign="middle">
              <Grid.Row>
                <Grid.Column width={8}>
                  {this.renderTextContent(content1)}
                  {this.renderTextContent(content2)}
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
                  {this.renderTextContent(content3)}
                </Grid.Column>
                <Grid.Column style={{ paddingBottom: '5em', paddingTop: '5em' }}>
                  {this.renderTextContent(content4)}
                </Grid.Column>
              </Grid.Row>
            </Grid>
          </Segment>

          <Segment style={{ padding: '8em 0em' }} vertical>
            <Container text>
              {this.renderHeader('Linked to WordNet, PropBank, and FrameNet')}
              <List size="large">
                {this.renderListItem(link1)}
                {this.renderListItem(link2)}
                {this.renderListItem(link3)}
              </List>
              <Button as="a" basic primary size="large" href="https://verbs.colorado.edu/semlink/">Read More</Button>
            </Container>
          </Segment>

          <Segment inverted vertical style={{ padding: '5em 0em' }}>
            <Container>
              <Grid divided inverted stackable>
                <Grid.Row>
                  <Grid.Column width={3}>
                    <Header inverted as="h4" content="VerbNet Resources" />
                    <List link inverted>
                      <List.Item as="a" href="http://verbs.colorado.edu/verb-index/vn3.3/">Unified Verb Index</List.Item>
                      <List.Item as="a" href="https://uvi.colorado.edu/">Unified Verb Index 2.0</List.Item>
                      <List.Item as="a" href="http://verbs.colorado.edu/semlink/">SemLink</List.Item>
                      <List.Item as="a" href="https://github.com/kevincstowe/verbnet">GitHub</List.Item>
                    </List>
                  </Grid.Column>
                  <Grid.Column width={3}>
                    <Header inverted as="h4" content="Applications" />
                    <List link inverted>
                      <List.Item as="a" href="https://github.com/jgung/verbnet-parser">VerbNet Semantic Parsing</List.Item>
                      <List.Item as="a" href="https://github.com/clearwsd/clearwsd">VerbNet Sense Disambiguation</List.Item>
                    </List>
                  </Grid.Column>
                  <Grid.Column width={7}>
                    <Header as="h4" inverted>Contact Us</Header>
                    <List link inverted>
                      <List.Item as="a" href="https://www.colorado.edu/lab/clear/contact-us">
                          Center for Computational Language and Education Research
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
