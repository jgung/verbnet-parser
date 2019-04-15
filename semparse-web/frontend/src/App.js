import PropTypes from 'prop-types'
import React, {Component} from 'react';
import _ from 'lodash';
import {
    Accordion,
    Button,
    Card,
    Container,
    Divider,
    Form,
    Grid,
    Header,
    Icon,
    Image,
    Label,
    List,
    Menu,
    Message,
    Responsive,
    Segment,
    Sidebar,
    Tab,
    Visibility,
} from 'semantic-ui-react'
import './App.css';
import clearLogo from './clear.png';

const MAX_LENGTH = 512;

// Heads up!
// We using React Static to prerender our docs with server side rendering, this is a quite simple solution.
// For more advanced usage please check Responsive docs under the "Usage" section.
const getWidth = () => {
    const isSSR = typeof window === 'undefined';

    return isSSR ? Responsive.onlyTablet.minWidth : window.innerWidth
};

const examples = [
    'If you liked the music we were playing last night, you will absolutely love what we\'re playing tomorrow!',
    'I didn\'t want to spend the next thirty years writing about bad things happening in the same small town',
    'John ate an apple and dropped the core into the trash',
    'Alice sold Bob her laptop for $300',
    'Where did John take the train after leaving the office?',
    'To whom did John lend his guitar last week?',
    'Alice went to the corner shop to buy some eggs, milk and bread for breakfast',
    'Mary discovered the impending layoffs from taking a look at her boss\'s computer',
    'In 2018, crude futures rose 74 cents, or 1.4 percent, to $53.95 from $53.21',
];

function randomExample() {
    return _.sample(examples);
}

const Span = ({sense, span, color = 'blue'}) => {
    const {label, text, isPredicate} = span;
    return <List.Item>
        <Label color={isPredicate ? 'blue' : color} basic={!isPredicate} size='large' content={isPredicate ? sense : label}
               detail={text}/>
    </List.Item>;
};

const Argument = ({arg}) => {
    const {type, value} = arg;
    return <List.Item header={type} content={value}/>;
};

const Predicate = ({pred}) => {
    const {type, args, polarity} = pred;
    return (
        <Card>
            <Card.Content>
                <Card.Header>{type}</Card.Header>
                {
                    !polarity && <Card.Meta>Negated</Card.Meta>
                }
                <List>
                    {args.map((arg, index) => <Argument key={arg.value + index} arg={arg}/>)}
                </List>
            </Card.Content>
        </Card>
    );
};

class Proposition extends Component {

    state = {
        tabIndex: 0
    };

    render() {
        const {showPropBank, showVerbNet, showSemantics, prop} = this.props;
        const {sense, events, propBankSpans, verbNetSpans} = prop;
        const pbSpans = propBankSpans.map(span => <Span sense={sense} key={span.start} span={span} color='black'/>);
        const vnSpans = verbNetSpans.map(span => <Span sense={sense} key={span.end} span={span} color='black'/>);

        const panes = events.map(event => {
          return {
            menuItem: event.name, render: () => (
              <Card.Group>
                {
                  event.predicates.map((pred, index) =>
                      <Predicate key={pred.predicate + index} pred={pred}/>)
                }
              </Card.Group>
            )
          }
        });

        return (
            <div>
                {
                    showPropBank && <List>
                        {pbSpans}
                    </List>
                }
                {
                    showPropBank && <Divider hidden/>
                }
                {
                    showVerbNet && <List>
                        {vnSpans}
                    </List>
                }
                {
                    (showPropBank || showVerbNet) && <Divider hidden/>
                }
                {
                    showSemantics && <Tab  menu={{ fluid: true }} panes={panes}/>
                }
            </div>
        );
    }
}

class PredictionResult extends Component {

    state = {
        activeIndex: 0,
        showOptions: false,
        showPropBank: false,
        showVerbNet: true,
        showSemantics: true,
    };

    handleTabChange = (e, {activeIndex}) => {
        this.setState({activeIndex});
    };

    setTabIndex = (activeIndex) => {
        this.setState({activeIndex});
    };

    togglePropBank = () => {
        this.setState({showPropBank: !this.state.showPropBank})
    };

    toggleVerbNet = () => {
        this.setState({showVerbNet: !this.state.showVerbNet})
    };

    toggleSemantics = () => {
        this.setState({showSemantics: !this.state.showSemantics})
    };

    toggleOptions = () => {
        this.setState({showOptions: !this.state.showOptions})
    };

    render() {
        if (!this.props.success) {
            if (this.props.failure && this.props.errorMessage) {
                return <Message negative content={this.props.errorMessage}/>;
            }
            return '';
        }
        const {tokens, props} = this.props.result;
        if (!props) {
            return ''
        }

        const propositions = props.map((prop, idx) => {
            const {sense} = prop;
            return {
                menuItem: sense,
                render: () => (
                    <Tab.Pane key={sense + idx} attached={false}>
                        <Proposition
                            showPropBank={this.state.showPropBank}
                            showVerbNet={this.state.showVerbNet}
                            showSemantics={this.state.showSemantics}
                            prop={prop}
                        />
                    </Tab.Pane>
                )
            };
        });

        const propIndex = Math.min(this.state.activeIndex, propositions.length - 1);

        const sentence = tokens.map((token, idx) => {
            const {text, label, isPredicate, index} = token;
            if (isPredicate) {
                return (
                    <Label
                        key={text + idx}
                        as='a'
                        size='medium'
                        color='blue'
                        basic={index !== propIndex}
                        content={text}
                        detail={label}
                        onClick={() => this.setTabIndex(index)}
                    />
                );
            }
            return ' ' + text + ' ';
        });

        return (
            <Segment>
                <Message style={{flexDirection: 'row'}}>
                    {sentence}
                </Message>
                <Accordion fluid>
                    <Accordion.Title active={this.state.showOptions} index={0} onClick={this.toggleOptions}>
                        <Icon name="dropdown"/>
                        View options
                    </Accordion.Title>
                    <Accordion.Content active={this.state.showOptions}>
                        <Button.Group>
                            <Button primary={this.state.showPropBank} content='PropBank' onClick={this.togglePropBank}/>
                            <Button primary={this.state.showVerbNet} content='VerbNet' onClick={this.toggleVerbNet}/>
                            <Button primary={this.state.showSemantics} content='Semantics' onClick={this.toggleSemantics}/>
                        </Button.Group>
                    </Accordion.Content>
                </Accordion>
                {
                    <Tab
                        menu={{secondary: true, pointing: true}}
                        activeIndex={propIndex}
                        onTabChange={this.handleTabChange}
                        panes={propositions}
                    />
                }
            </Segment>
        );
    };
}

/* eslint-disable react/no-multi-comp */

/* Heads up! HomepageHeading uses inline styling, however it's not the best practice. Use CSS or styled components for
 * such things.
 */
class HomepageHeading extends Component {

    state = {
        utterance: '',
        message: '',
        success: false,
        failure: false,
        loading: false,
        errorMessage: ''
    };

    submitUtterance = () => {
        const utterance = !this.state.utterance ? randomExample() : this.state.utterance;
        if (utterance.length > MAX_LENGTH) {
            this.setState({
                              failure: true,
                              errorMessage: `Sorry, the maximum utterance length is ${MAX_LENGTH} characters. Please try a shorter sentence.`,
                              message: '',
                              success: false,
                          })
        } else {
            this.setState({success: false, failure: false, loading: true});
            fetch(`/predict/semantics?utterance=${encodeURIComponent(utterance)}`)
                .then(response => {
                          if (response.ok) {
                              return response.json();
                          } else {
                              throw new Error('Something went wrong');
                          }
                      },
                      ignored => {
                          this.setState({
                                            success: false,
                                            failure: true,
                                            loading: false,
                                            message: '',
                                            errorMessage: "Sorry, we're unable to make predictions at this time."
                                        })
                      }
                )
                .then(message => {
                    this.setState({success: true, failure: false, loading: false, message: message});
                })
                .catch((ignored) => {
                    this.setState({
                                      success: false,
                                      failure: true,
                                      loading: false,
                                      message: '',
                                      errorMessage: "Sorry, something went wrong and we were unable to handle your request."
                                  });
                })
        }
    };

    updateUtterance = (e) => {
        this.setState({...this.state, utterance: e.target.value})
    };

    render() {
        const {mobile} = this.props;
        return (<Container text>
                <Header
                    as='h1'
                    textAlign='center'
                    content='VerbNet Parser'
                    inverted
                    style={{
                        fontSize: mobile ? '2em' : '4em',
                        fontWeight: 'normal',
                        marginBottom: 0,
                        marginTop: mobile ? '1.5em' : '3em',
                    }}
                />
                <Form loading={this.state.loading} onSubmit={this.submitUtterance}>
                    <Form.Input
                        size='huge'
                        placeholder='Type your own sentence, or see a random example...'
                        action={<Button disabled={this.state.loading} primary size='huge' onClick={this.submitUtterance}
                                        content='Try it out!'/>}
                        onChange={this.updateUtterance}
                    />
                </Form>
                <PredictionResult
                    result={this.state.message}
                    success={this.state.success}
                    failure={this.state.failure}
                    errorMessage={this.state.errorMessage}
                    loading={this.state.loading}
                />
            </Container>
        );
    }
}

HomepageHeading.propTypes = {
    mobile: PropTypes.bool,
};

class DesktopContainer extends Component {
    state = {};

    hideFixedMenu = () => this.setState({fixed: false});
    showFixedMenu = () => this.setState({fixed: true});

    render() {
        const {children} = this.props;
        const {fixed} = this.state;

        return (
            <Responsive getWidth={getWidth} minWidth={Responsive.onlyTablet.minWidth}>
                <Visibility
                    once={false}
                    onBottomPassed={this.showFixedMenu}
                    onBottomPassedReverse={this.hideFixedMenu}
                >
                    <Segment
                        inverted
                        textAlign='left'
                        style={{minHeight: 700, padding: '1em 0em'}}
                        vertical
                    >
                        <Menu
                            fixed={fixed ? 'top' : null}
                            inverted={!fixed}
                            pointing={!fixed}
                            secondary={!fixed}
                            size='large'
                        >
                            <Container>
                                <Menu.Item as='a' active>
                                    Demo
                                </Menu.Item>
                                <Menu.Item as='a' href='https://uvi.colorado.edu/'>VerbNet</Menu.Item>
                                <Menu.Item as='a' href='https://www.colorado.edu/lab/clear/'>CLEAR @ Colorado</Menu.Item>
                                <Menu.Item position='right' as='a' href='https://github.com/jgung/verbnet-parser'>
                                    <Icon name='github'/>
                                    GitHub
                                </Menu.Item>
                            </Container>
                        </Menu>
                        <HomepageHeading/>
                    </Segment>
                </Visibility>
                {children}
            </Responsive>
        )
    }
}

DesktopContainer.propTypes = {
    children: PropTypes.node,
};

class MobileContainer extends Component {
    state = {};

    handleSidebarHide = () => this.setState({sidebarOpened: false});

    handleToggle = () => this.setState({sidebarOpened: true});

    render() {
        const {children} = this.props;
        const {sidebarOpened} = this.state;

        return (
            <Responsive
                as={Sidebar.Pushable}
                getWidth={getWidth}
                maxWidth={Responsive.onlyMobile.maxWidth}
            >
                <Sidebar
                    as={Menu}
                    animation='push'
                    inverted
                    onHide={this.handleSidebarHide}
                    vertical
                    visible={sidebarOpened}
                >
                    <Menu.Item as='a' active>Demo</Menu.Item>
                    <Menu.Item as='a' href='https://uvi.colorado.edu/'>VerbNet</Menu.Item>
                    <Menu.Item as='a' href='https://www.colorado.edu/lab/clear/'>CLEAR @ Colorado</Menu.Item>
                </Sidebar>

                <Sidebar.Pusher dimmed={sidebarOpened}>
                    <Segment
                        inverted
                        textAlign='center'
                        style={{minHeight: 350, padding: '1em 0em'}}
                        vertical
                    >
                        <Container>
                            <Menu inverted pointing secondary size='large'>
                                <Menu.Item onClick={this.handleToggle}>
                                    <Icon name='sidebar'/>
                                </Menu.Item>
                                <Menu.Item position='right' as='a' href='https://github.com/jgung/verbnet-parser'>
                                    <Icon name='github'/>
                                    GitHub
                                </Menu.Item>
                            </Menu>
                        </Container>
                        <HomepageHeading mobile/>
                    </Segment>

                    {children}
                </Sidebar.Pusher>
            </Responsive>
        )
    }
}

MobileContainer.propTypes = {
    children: PropTypes.node,
};

const ResponsiveContainer = ({children}) => (
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
                <Segment style={{padding: '8em 0em'}} vertical>
                    <Grid container stackable verticalAlign='middle'>
                        <Grid.Row>
                            <Grid.Column width={8}>
                                <Header as='h3' style={{fontSize: '2em'}}>
                                    A Class-Based Verb Lexicon
                                </Header>
                                <p style={{fontSize: '1.33em'}}>
                                    VerbNet provides a hierarchical, domain-independent broad-coverage verb lexicon with mappings to
                                    other lexical resources.
                                </p>
                                <Header as='h3' style={{fontSize: '2em'}}>
                                    329 verb classes and over 270 subclasses
                                </Header>
                                <p style={{fontSize: '1.33em'}}>
                                    6,791 unique verb senses with corresponding thematic roles and entailed open-domain semantic
                                    predicates.
                                </p>
                            </Grid.Column>
                            <Grid.Column floated='right' width={6}>
                                <Image href='https://www.colorado.edu/lab/clear/' rounded size='large' src={clearLogo}/>
                            </Grid.Column>
                        </Grid.Row>
                        <Grid.Row>
                            <Grid.Column textAlign='center'>
                                <Button primary size='huge' href='https://uvi.colorado.edu/'>Check it out!</Button>
                            </Grid.Column>
                        </Grid.Row>
                    </Grid>
                </Segment>

                <Segment style={{padding: '0em'}} vertical>
                    <Grid celled='internally' columns='equal' stackable>
                        <Grid.Row textAlign='center'>
                            <Grid.Column style={{paddingBottom: '5em', paddingTop: '5em'}}>
                                <Header as='h3' style={{fontSize: '2em'}}>
                                    Over 150 open-domain semantic predicates
                                </Header>
                                <p style={{fontSize: '1.33em'}}>
                                    Predicates provide shared event semantics across classes grounded in a fixed set of semantic
                                    primitives.
                                </p>
                            </Grid.Column>
                            <Grid.Column style={{paddingBottom: '5em', paddingTop: '5em'}}>
                                <Header as='h3' style={{fontSize: '2em'}}>
                                    2,168 syntactic frames with associated semantic predicates
                                </Header>
                                <p style={{fontSize: '1.33em'}}>
                                    Each class contains a set of syntactic descriptions, or syntactic frames, depicting the
                                    possible surface realizations of argument structures.
                                </p>
                            </Grid.Column>
                        </Grid.Row>
                    </Grid>
                </Segment>

                <Segment style={{padding: '8em 0em'}} vertical>
                    <Container text>
                        <Header as='h3' style={{fontSize: '2em'}}>
                            Linked to WordNet, PropBank, and FrameNet
                        </Header>
                        <List size='large'>
                            <List.Item>
                                <List.Header as='a' href='https://wordnet.princeton.edu/'>WordNet</List.Header>
                                <List.Description>
                                    A lexicon that describes semantic relationships (such as synonymy and hyperonymy) between
                                    individual words.
                                </List.Description>
                            </List.Item>
                            <List.Item>
                                <List.Header as='a' href='https://propbank.github.io/'>PropBank</List.Header>
                                <List.Description>
                                    A corpus of one million words of English text, annotated with argument role labels for verbs;
                                    and a lexicon defining those argument roles on a per-verb basis.
                                </List.Description>
                            </List.Item>
                            <List.Item>
                                <List.Header as='a' href='https://framenet.icsi.berkeley.edu/fndrupal/'>FrameNet</List.Header>
                                <List.Description>
                                    A lexicon based on frame semantics.
                                </List.Description>
                            </List.Item>
                        </List>
                        <Button as='a' basic primary size='large' href='https://verbs.colorado.edu/semlink/'>
                            Read More
                        </Button>
                    </Container>
                </Segment>

                <Segment inverted vertical style={{padding: '5em 0em'}}>
                    <Container>
                        <Grid divided inverted stackable>
                            <Grid.Row>
                                <Grid.Column width={3}>
                                    <Header inverted as='h4' content='VerbNet Resources'/>
                                    <List link inverted>
                                        <List.Item as='a' href='http://verbs.colorado.edu/verb-index/vn3.3/'>Unified Verb
                                            Index</List.Item>
                                        <List.Item as='a' href='https://uvi.colorado.edu/'>Unified Verb Index 2.0</List.Item>
                                        <List.Item as='a' href='http://verbs.colorado.edu/semlink/'>SemLink</List.Item>
                                        <List.Item as='a' href='https://github.com/kevincstowe/verbnet'>GitHub</List.Item>
                                    </List>
                                </Grid.Column>
                                <Grid.Column width={3}>
                                    <Header inverted as='h4' content='Applications'/>
                                    <List link inverted>
                                        <List.Item as='a' href='https://github.com/jgung/verbnet-parser'>VerbNet Semantic
                                            Parsing</List.Item>
                                        <List.Item as='a' href='https://github.com/clearwsd/clearwsd'>VerbNet Sense
                                            Disambiguation</List.Item>
                                    </List>
                                </Grid.Column>
                                <Grid.Column width={7}>
                                    <Header as='h4' inverted>
                                        Contact Us
                                    </Header>
                                    <List link inverted>
                                        <List.Item as='a' href='https://www.colorado.edu/lab/clear/contact-us'>Center for
                                            Computational Language and Education Research</List.Item>
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

export default App
